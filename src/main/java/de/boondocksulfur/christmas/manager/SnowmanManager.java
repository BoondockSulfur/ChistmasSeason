package de.boondocksulfur.christmas.manager;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import de.boondocksulfur.christmas.ChristmasSeason;
import de.boondocksulfur.christmas.util.LanguageManager;
import de.boondocksulfur.christmas.util.SpawnUtil;
import de.boondocksulfur.christmas.util.FoliaSchedulerHelper;

import java.util.List;
import java.util.Random;

public class SnowmanManager {

    public static final String TAG = "XMAS_SNOWMAN";

    private final ChristmasSeason plugin;
    private final LanguageManager lang;
    private final FoliaSchedulerHelper scheduler;
    private final Random random = new Random();

    // FOLIA FIX: Player-basierte Spawn-Timer (Entity Scheduler)
    private final java.util.Map<java.util.UUID, WrappedTask> playerSpawnTasks = new java.util.concurrent.ConcurrentHashMap<>();

    // FOLIA FIX: Entity-basierte Attack-Tasks (Entity Scheduler pro Schneemann)
    private final java.util.Map<java.util.UUID, WrappedTask> entityAttackTasks = new java.util.concurrent.ConcurrentHashMap<>();

    public SnowmanManager(ChristmasSeason plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLanguageManager();
        this.scheduler = new FoliaSchedulerHelper(plugin);
    }

    public void start() {
        stop();
        if (!plugin.getConfig().getBoolean("snowmen.enabled", true)) return;

        // FOLIA FIX: Spawn-Timer sind jetzt Player-basiert (siehe startPlayerSpawning)
        // FOLIA FIX: Attack-Tasks sind jetzt Entity-basiert (siehe startEntityAttackTask)
        plugin.debug("SnowmanManager gestartet (Folia-kompatibel: Player-basierte Spawns + Entity-basierte AI)");
    }

    public void stop() {
        // FOLIA FIX: Stoppe alle Player-basierten Tasks
        for (WrappedTask task : playerSpawnTasks.values()) {
            if (task != null) task.cancel();
        }
        playerSpawnTasks.clear();

        // FOLIA FIX: Stoppe alle Entity-basierten Attack-Tasks
        for (WrappedTask task : entityAttackTasks.values()) {
            if (task != null) task.cancel();
        }
        entityAttackTasks.clear();
    }

    /**
     * Startet Schneemann-Spawning für einen Spieler (Entity Scheduler)
     * FOLIA-KOMPATIBEL: Läuft auf Entity Scheduler des Players
     */
    public void startPlayerSpawning(Player player) {
        if (!plugin.getConfig().getBoolean("snowmen.enabled", true)) return;

        java.util.UUID uuid = player.getUniqueId();
        WrappedTask oldTask = playerSpawnTasks.remove(uuid);
        if (oldTask != null) oldTask.cancel();

        int interval = plugin.getConfig().getInt("snowmen.spawnIntervalSeconds", 30);
        WrappedTask task = scheduler.runForEntityTimer(player, () -> {
            if (!player.isOnline() || !player.isValid()) {
                stopPlayerSpawning(player);
                return;
            }
            spawnSnowmanNearPlayer(player);
        }, 40L, interval * 20L);

        if (task != null) {
            playerSpawnTasks.put(uuid, task);
            plugin.debug("Schneemann-Spawning gestartet für " + player.getName());
        }
    }

    /**
     * Stoppt Schneemann-Spawning für einen Spieler
     */
    public void stopPlayerSpawning(Player player) {
        WrappedTask task = playerSpawnTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
            plugin.debug("Schneemann-Spawning gestoppt für " + player.getName());
        }
    }

    /** Entfernt alle Schneemänner aus der Welt */
    public void cleanup() {
        String worldName = plugin.getConfig().getString("snowWorld", "world");
        World w = Bukkit.getWorld(worldName);
        if (w == null) {
            plugin.getLogger().warning("Schneemann-Cleanup: Welt nicht gefunden!");
            return;
        }

        int removed = 0;
        for (Snowman sm : w.getEntitiesByClass(Snowman.class)) {
            if (sm.getScoreboardTags().contains(TAG)) {
                sm.remove();
                removed++;
            }
        }
        plugin.getLogger().info(lang.getMessage("log.cleanup.snowmen", removed));
    }

    /**
     * Spawnt Schneemann in Nähe eines Spielers
     * FOLIA-KOMPATIBEL: Wird von Entity Scheduler des Players aufgerufen
     */
    private void spawnSnowmanNearPlayer(Player player) {
        World w = player.getWorld();
        String worldName = plugin.getConfig().getString("snowWorld", "world");
        if (!w.getName().equals(worldName)) return;

        // Zähle aktuelle Schneemänner (global limit)
        int current = 0;
        for (Snowman sm : w.getEntitiesByClass(Snowman.class)) {
            if (sm.getScoreboardTags().contains(TAG)) current++;
        }
        int max = plugin.getConfig().getInt("snowmen.maxPerWorld", 6);
        if (current >= max) return;

        // FOLIA FIX: Spawne auf Location Scheduler (für getHighestBlockAt)
        Location playerLoc = player.getLocation();

        scheduler.runAtLocation(playerLoc, () -> {
            // Safe-Spawn: 5 Versuche (Performance-optimiert, strenge Wasser/Wand-Checks)
            Location loc = SpawnUtil.findSafeSpawnLocation(w, playerLoc, 10, 5);

            Snowman sm = w.spawn(loc, Snowman.class);
            sm.setCustomName(lang.get("entity.snowman"));
            sm.setCustomNameVisible(true);
            sm.getScoreboardTags().add(TAG);
            sm.setDerp(false);

            // FOLIA FIX: Starte Entity Scheduler Task für Attack-Logik
            startEntityAttackTask(sm);
        });
    }

    /**
     * Startet einen Entity Scheduler Task für Attack-Logik
     * FOLIA-KOMPATIBEL: Läuft auf Entity Scheduler der Schneemann-Entity
     */
    private void startEntityAttackTask(Snowman snowman) {
        java.util.UUID snowmanId = snowman.getUniqueId();
        double range = plugin.getConfig().getDouble("snowmen.range", 12.0);
        double chance = plugin.getConfig().getDouble("snowmen.attackChance", 0.35);
        int attackInterval = plugin.getConfig().getInt("snowmen.attackIntervalSeconds", 5);

        // FOLIA FIX: Entity Scheduler Task für diesen spezifischen Schneemann
        WrappedTask task = scheduler.runForEntityTimer(snowman, () -> {
            // Entity-State-Zugriff ist sicher, weil wir auf Entity Scheduler laufen!
            if (!snowman.isValid() || snowman.isDead()) {
                WrappedTask oldTask = entityAttackTasks.remove(snowmanId);
                if (oldTask != null) oldTask.cancel();
                return;
            }

            // Attack-Chance prüfen
            if (random.nextDouble() > chance) return;

            // Finde nächsten Spieler in Range
            Player target = null;
            double bestDistSq = Double.MAX_VALUE;
            World w = snowman.getWorld();

            for (Player p : w.getPlayers()) {
                double distSq = p.getLocation().distanceSquared(snowman.getLocation());
                if (distSq <= range * range && distSq < bestDistSq) {
                    bestDistSq = distSq;
                    target = p;
                }
            }

            if (target == null) return;

            // Richtungsvektor berechnen und auf NaN prüfen (falls Schneemann und Spieler auf gleicher Position)
            org.bukkit.util.Vector direction = target.getLocation().toVector()
                    .subtract(snowman.getLocation().toVector());

            // Wenn die Entfernung zu klein ist, überspringe den Angriff
            if (direction.lengthSquared() < 0.01) return;

            direction.normalize().multiply(1.1);

            // Schneeball abfeuern
            Snowball ball = snowman.launchProjectile(Snowball.class);
            ball.setCustomName("XMAS_SNOWBALL");
            ball.setVelocity(direction);

        }, 60L, attackInterval * 20L);

        if (task != null) {
            entityAttackTasks.put(snowmanId, task);
        }
    }
}
