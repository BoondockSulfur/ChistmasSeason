package de.boondocksulfur.christmas.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import de.boondocksulfur.christmas.ChristmasSeason;
import de.boondocksulfur.christmas.util.LanguageManager;
import de.boondocksulfur.christmas.util.SpawnUtil;
import de.boondocksulfur.christmas.util.FoliaSchedulerHelper;

import java.util.List;
import java.util.Random;

public class DecorationManager {

    private final ChristmasSeason plugin;
    private final LanguageManager lang;
    private final FoliaSchedulerHelper scheduler;
    private final Random random = new Random();

    // FOLIA FIX: Player-basierte Spawn-Timer (Entity Scheduler)
    private final java.util.Map<java.util.UUID, WrappedTask> playerSpawnTasks = new java.util.concurrent.ConcurrentHashMap<>();

    public DecorationManager(ChristmasSeason plugin) {
        this.plugin = plugin;
        this.lang = plugin.getLanguageManager();
        this.scheduler = new FoliaSchedulerHelper(plugin);
    }

    public void start() {
        stop();
        // FOLIA FIX: Spawn-Timer sind jetzt Player-basiert (siehe startPlayerSpawning)
        plugin.debug("DecorationManager gestartet (Folia-kompatibel: Player-basierte Spawns)");
    }

    public void stop() {
        // FOLIA FIX: Stoppe alle Player-basierten Tasks
        for (WrappedTask task : playerSpawnTasks.values()) {
            if (task != null) task.cancel();
        }
        playerSpawnTasks.clear();
    }

    /**
     * Startet Dekorations-Spawning für einen Spieler (Entity Scheduler)
     * FOLIA-KOMPATIBEL: Läuft auf Entity Scheduler des Players
     */
    public void startPlayerSpawning(org.bukkit.entity.Player player) {
        if (!plugin.getConfig().getBoolean("decoration.enabled", true)) return;

        java.util.UUID uuid = player.getUniqueId();
        WrappedTask oldTask = playerSpawnTasks.remove(uuid);
        if (oldTask != null) oldTask.cancel();

        int interval = plugin.getConfig().getInt("decoration.intervalSeconds", 25);
        double spawnChance = plugin.getConfig().getDouble("decoration.spawnChance", 0.9);

        WrappedTask task = scheduler.runForEntityTimer(player, () -> {
            if (!player.isOnline() || !player.isValid()) {
                stopPlayerSpawning(player);
                return;
            }
            if (random.nextDouble() <= spawnChance) {
                spawnDecorationNearPlayer(player);
            }
        }, 40L, interval * 20L);

        if (task != null) {
            playerSpawnTasks.put(uuid, task);
            plugin.debug("Dekorations-Spawning gestartet für " + player.getName());
        }
    }

    /**
     * Stoppt Dekorations-Spawning für einen Spieler
     */
    public void stopPlayerSpawning(org.bukkit.entity.Player player) {
        WrappedTask task = playerSpawnTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
            plugin.debug("Dekorations-Spawning gestoppt für " + player.getName());
        }
    }

    /** Entfernt alle Dekorations-Items aus der Welt */
    public void cleanup() {
        String worldName = plugin.getConfig().getString("snowWorld", "world");
        World w = Bukkit.getWorld(worldName);
        if (w == null) {
            plugin.getLogger().warning(lang.getMessage("log.cleanup.world-not-found", "Decoration"));
            return;
        }

        String name = lang.get("entity.decoration");
        int removed = 0;

        // OPTIMIERT: Verwende getEntitiesByClass statt w.getEntities() - viel schneller!
        for (Item item : w.getEntitiesByClass(Item.class)) {
            if (item.getCustomName() != null && item.getCustomName().equals(name)) {
                item.remove();
                removed++;
            }
        }
        plugin.getLogger().info(lang.getMessage("log.cleanup.decorations", removed));
    }

    /**
     * Spawnt Dekoration in Nähe eines Spielers
     * FOLIA-KOMPATIBEL: Wird von Entity Scheduler des Players aufgerufen
     */
    private void spawnDecorationNearPlayer(Player player) {
        World w = player.getWorld();
        String worldName = plugin.getConfig().getString("snowWorld", "world");
        if (!w.getName().equals(worldName)) return;

        List<String> drops = plugin.getConfig().getStringList("decoration.drops");
        if (drops.isEmpty()) return;

        // FOLIA FIX: Spawne auf Location Scheduler (für findSurface und dropItem)
        Location playerLoc = player.getLocation();

        scheduler.runAtLocation(playerLoc, () -> {
            // Safe-Spawn: 5 Versuche (Performance-optimiert, strenge Wasser/Wand-Checks)
            Location place = SpawnUtil.findSafeSpawnLocation(w, playerLoc, 7, 5).add(0, 0.5, 0);

            String entry = drops.get(random.nextInt(drops.size()));
            String[] split = entry.split(":");
            Material mat = Material.matchMaterial(split[0]);
            if (mat == null) return;
            int amount = 1;
            if (split.length > 1) try { amount = Integer.parseInt(split[1]); } catch (NumberFormatException ignored) {}

            ItemStack stack = new ItemStack(mat, amount);
            String name = lang.get("entity.decoration");
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) { meta.setDisplayName(name); stack.setItemMeta(meta); }

            Item item = w.dropItem(place, stack);
            item.setCustomName(name);
            item.setCustomNameVisible(true);
            item.setPickupDelay(plugin.getConfig().getInt("decoration.pickupDelayTicks", 0));
            try { item.setGlowing(plugin.getConfig().getBoolean("decoration.glow", true)); } catch (Throwable ignored) {}

            int lifetime = plugin.getConfig().getInt("decoration.lifetimeSeconds", 180);
            scheduler.runForEntityLater(item, () -> { if (!item.isDead() && item.isValid()) item.remove(); }, lifetime * 20L);
        });
    }
}
