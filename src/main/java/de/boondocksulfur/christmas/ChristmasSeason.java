package de.boondocksulfur.christmas;

import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import de.boondocksulfur.christmas.cmd.XmasCommand;
import de.boondocksulfur.christmas.cmd.XmasGiftCommand;
import de.boondocksulfur.christmas.listener.*;
import de.boondocksulfur.christmas.manager.*;
import de.boondocksulfur.christmas.util.LanguageManager;

public class ChristmasSeason extends JavaPlugin {

    private LanguageManager languageManager;
    private SnowstormManager snowstormManager;
    private BiomeSnowManager biomeSnowManager;
    private DecorationManager decorationManager;
    private GiftManager giftManager;
    private WichtelManager wichtelManager;
    private SnowmanManager snowmanManager;
    private BiomeSnapshotBackup backupManager;
    private de.boondocksulfur.christmas.util.UpdateChecker updateChecker;
    private de.boondocksulfur.christmas.manager.BiomeCompare biomeCompare;
    private de.boondocksulfur.christmas.integration.RegionIntegration regionIntegration;

    // Debug-Modus für ausführliche Logs
    private boolean debugMode = false;
    private boolean verboseDebugMode = false; // Noch ausführlichere Logs (Biome-Snapshot Details)

    @Override
    public void onEnable() {
        saveDefaultConfig();

        // Sprachdateien extrahieren falls nicht vorhanden
        saveResourceIfAbsent("messages_de.yml");
        saveResourceIfAbsent("messages_en.yml");

        this.languageManager   = new LanguageManager(this);
        this.backupManager     = new BiomeSnapshotBackup(this);
        this.updateChecker     = new de.boondocksulfur.christmas.util.UpdateChecker(this);
        this.biomeCompare      = new de.boondocksulfur.christmas.manager.BiomeCompare(this);
        this.snowstormManager  = new SnowstormManager(this);
        this.biomeSnowManager  = new BiomeSnowManager(this);
        this.decorationManager = new DecorationManager(this);
        this.giftManager       = new GiftManager(this);
        this.wichtelManager    = new WichtelManager(this);
        this.snowmanManager    = new SnowmanManager(this);
        this.regionIntegration = new de.boondocksulfur.christmas.integration.RegionIntegration(this);

        XmasCommand xmasCommand = new XmasCommand(this);
        getCommand("xmas").setExecutor(xmasCommand);
        getCommand("xmas").setTabCompleter(xmasCommand);
        getCommand("xmasgift").setExecutor(new XmasGiftCommand(this));

        Bukkit.getPluginManager().registerEvents(new GiftOpenListener(this), this);
        Bukkit.getPluginManager().registerEvents(new WichtelTargetBlocker(), this);
        Bukkit.getPluginManager().registerEvents(new SnowmanDamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new MobProtectionListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChunkSnowListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerSnowBubbleListener(this), this);
        Bukkit.getPluginManager().registerEvents(new de.boondocksulfur.christmas.listener.UpdateNotificationListener(this), this);

        // bStats Metrics
        new Metrics(this, 30930);

        // Startup-Sicherheitsprüfungen
        performStartupSafetyChecks();

        if (isActive()) startFeatures();

        // Auto-Update-Check beim Server-Start
        updateChecker.startAutoCheck();

        getLogger().info("ChristmasSeason enabled.");
    }

    @Override
    public void onDisable() {
        // NOTFALL-BACKUP: Wenn Server stoppt während xmas ON aktiv ist!
        if (isActive() && backupManager != null) {
            getLogger().warning("Server wird gestoppt während ChristmasSeason AKTIV ist!");
            getLogger().warning("Erstelle Notfall-Backup der Biome-Datenbank...");
            backupManager.createEmergencyBackup();
        }

        stopFeatures();
    }

    public boolean isActive() { return getConfig().getBoolean("active", false); }

    public void startFeatures() {
        snowstormManager.start();
        biomeSnowManager.start();
        decorationManager.start();
        giftManager.start();
        wichtelManager.start();
        snowmanManager.start();

        // FOLIA FIX: Starte Player-basierte Tasks für bereits online Spieler
        // (PlayerJoinEvent wird nur für neue Joins gefeuert, nicht für bereits online Spieler!)
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            biomeSnowManager.startPlayerTracking(player);
            wichtelManager.startPlayerSpawning(player);
            snowmanManager.startPlayerSpawning(player);
            giftManager.startPlayerSpawning(player);
            decorationManager.startPlayerSpawning(player);
            debug("Player-Tracking für bereits online Spieler gestartet: " + player.getName());
        }
    }
    public void stopFeatures() {
        stopFeatures(true);
    }

    /** Stop Features mit optionalem Biome-DB schließen */
    public void stopFeatures(boolean closeBiomeDatabase) {
        // FIX: Null-Checks für den Fall dass onEnable() fehlgeschlagen ist
        if (snowstormManager != null) snowstormManager.stop();
        if (biomeSnowManager != null) biomeSnowManager.stop(closeBiomeDatabase);
        if (decorationManager != null) decorationManager.stop();
        if (giftManager != null) giftManager.stop();
        if (wichtelManager != null) wichtelManager.stop();
        if (snowmanManager != null) snowmanManager.stop();
    }
    public void reloadAll() {
        reloadConfig();
        languageManager.reload();
        stopFeatures();
        if (isActive()) startFeatures();
    }

    /**
     * Führt Sicherheitsprüfungen beim Start durch:
     * - DB-Integritätscheck
     * - Warnung bei active:true ohne DB
     * - Erkennung von Emergency-Backups (vorheriger Crash)
     */
    private void performStartupSafetyChecks() {
        java.io.File dbFile = new java.io.File(getDataFolder(), "biome-snapshot.db");

        // Check 1: active:true aber keine DB → Warnung
        if (isActive() && !dbFile.exists() && getConfig().getBoolean("biome.enableSnapshot", true)) {
            getLogger().warning("§c═══════════════════════════════════════════");
            getLogger().warning("§c§l WARNUNG: ChristmasSeason ist aktiv, aber keine Snapshot-DB vorhanden!");
            getLogger().warning("§c Biome wurden möglicherweise geändert und können nicht restored werden.");
            getLogger().warning("§c Prüfe: /xmas backup list (für verfügbare Backups)");
            getLogger().warning("§c═══════════════════════════════════════════");
        }

        // Check 2: DB-Integrität prüfen (falls DB existiert)
        if (dbFile.exists()) {
            try {
                Class.forName("org.sqlite.JDBC");
                try (java.sql.Connection conn = java.sql.DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
                     java.sql.Statement stmt = conn.createStatement();
                     java.sql.ResultSet rs = stmt.executeQuery("PRAGMA integrity_check")) {
                    if (rs.next()) {
                        String result = rs.getString(1);
                        if (!"ok".equalsIgnoreCase(result)) {
                            getLogger().severe("§c═══════════════════════════════════════════");
                            getLogger().severe("§c§l DATENBANK-KORRUPTION ERKANNT!");
                            getLogger().severe("§c Integrity Check: " + result);
                            getLogger().severe("§c Empfehlung: /xmas backup restore SAFE confirm");
                            getLogger().severe("§c═══════════════════════════════════════════");
                        } else {
                            debug("DB-Integritätscheck: OK");
                        }
                    }
                }
            } catch (Exception e) {
                getLogger().severe("§c DB-Integritätscheck fehlgeschlagen: " + e.getMessage());
                getLogger().severe("§c Die Datenbank könnte beschädigt sein. Prüfe: /xmas backup list");
            }
        }

        // Check 3: Emergency-Backups erkennen (Hinweis auf vorherigen Crash)
        if (backupManager != null) {
            java.util.Map<String, java.io.File> allBackups = backupManager.listAllBackups();
            long emergencyCount = allBackups.keySet().stream().filter(k -> k.startsWith("EMERGENCY")).count();
            if (emergencyCount > 0) {
                getLogger().warning("§e═══════════════════════════════════════════");
                getLogger().warning("§e " + emergencyCount + " Emergency-Backup(s) gefunden!");
                getLogger().warning("§e Der Server wurde zuvor gestoppt während ChristmasSeason aktiv war.");
                getLogger().warning("§e Prüfe: /xmas backup list → /xmas backup restore <ID> confirm");
                getLogger().warning("§e═══════════════════════════════════════════");
            }
        }
    }

    // Helper
    private void saveResourceIfAbsent(String fileName) {
        java.io.File file = new java.io.File(getDataFolder(), fileName);
        if (!file.exists()) {
            try {
                // Prüfe ob Ressource im JAR existiert
                java.io.InputStream resource = getResource(fileName);
                if (resource == null) {
                    getLogger().severe("Resource not found in JAR: " + fileName);
                    return;
                }
                resource.close();

                saveResource(fileName, false);
                getLogger().info("Extracted resource: " + fileName + " (Size: " + file.length() + " bytes)");
            } catch (Exception e) {
                getLogger().severe("Could not extract " + fileName + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            getLogger().info("Resource already exists: " + fileName + " (Size: " + file.length() + " bytes)");
        }
    }

    // Getters
    public LanguageManager getLanguageManager() { return languageManager; }
    public BiomeSnapshotBackup getBackupManager() { return backupManager; }
    public de.boondocksulfur.christmas.util.UpdateChecker getUpdateChecker() { return updateChecker; }
    public de.boondocksulfur.christmas.manager.BiomeCompare getBiomeCompare() { return biomeCompare; }
    public GiftManager getGiftManager() { return giftManager; }
    public BiomeSnowManager getBiomeSnowManager() { return biomeSnowManager; }
    public WichtelManager getWichtelManager() { return wichtelManager; }
    public SnowmanManager getSnowmanManager() { return snowmanManager; }
    public SnowstormManager getSnowstormManager() { return snowstormManager; }
    public DecorationManager getDecorationManager() { return decorationManager; }
    public de.boondocksulfur.christmas.integration.RegionIntegration getRegionIntegration() { return regionIntegration; }

    // Debug-Modus
    public boolean isDebugMode() { return debugMode; }
    public void setDebugMode(boolean enabled) { this.debugMode = enabled; }

    public boolean isVerboseDebugMode() { return verboseDebugMode; }
    public void setVerboseDebugMode(boolean enabled) {
        this.verboseDebugMode = enabled;
        if (enabled) this.debugMode = true; // Verbose aktiviert automatisch Debug
    }

    /** Debug-Log: Nur ausgeben wenn Debug-Modus aktiv */
    public void debug(String message) {
        if (debugMode) {
            getLogger().info("§8[DEBUG] §7" + message);
        }
    }

    /** Debug-Log mit Sprach-Unterstützung */
    public void debugLang(String key, Object... replacements) {
        if (debugMode) {
            String message = languageManager.getMessage(key, replacements);
            getLogger().info("§8[DEBUG] §7" + message);
        }
    }

    /** Verbose Debug-Log: Nur ausgeben wenn Verbose-Debug-Modus aktiv */
    public void verboseDebug(String message) {
        if (verboseDebugMode) {
            getLogger().info("§8[VERBOSE] §7" + message);
        }
    }

    /** Verbose Debug-Log mit Sprach-Unterstützung */
    public void verboseDebugLang(String key, Object... replacements) {
        if (verboseDebugMode) {
            String message = languageManager.getMessage(key, replacements);
            getLogger().info("§8[VERBOSE] §7" + message);
        }
    }
}
