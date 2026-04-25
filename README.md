# ChristmasSeason v2.2 🎄

**Transform your Minecraft world into a winter wonderland!**

A comprehensive Christmas plugin featuring biome snowfall, snowstorms, NPCs, gifts, and much more. Now with **Multi-Platform Support** for Spigot, Paper, Purpur, **and Folia**!

**NEW in v2.2:** WorldGuard & GriefPrevention Support, Backup System, Update Notifications, Tab Completion, bStats, and 17 Bug Fixes!

---

## 🌟 Features

### ❄️ Dynamic Biome System
- **Player-Bubble:** Snow biomes appear in a configurable radius around players
- **3D-Biome Changes:** Works with 1.18+ Multi-Y-Level Biomes
- **Automatic Restore:** Original biomes are saved in SQLite database
- **Smart Caching:** Performance-optimized with budget system (prevents TPS spikes)
- **Blacklist:** Nether/End/Cave biomes remain unchanged

### 🌨️ Snowstorms
- **Auto Mode:** Alternates between snowfall and sunshine
- **Manual Mode:** Permanent snowfall
- **None Mode:** Biome changes only, no weather

### 🎁 Interactive Elements
- **Gifts:** Randomly spawning chests with loot (common/extra/rare)
- **Decorations:** Glowing items spawn around players
- **Wichtel:** Mischievous mobs that can steal items
- **Elves:** Friendly NPCs that wander around
- **Snowmen:** Aggressive snow golems that throw snowballs

### 🛡️ Region Protection (NEW in v2.2!)
- **WorldGuard Support:** No spawns in protected regions
- **GriefPrevention Support:** No spawns in player claims
- **Soft Dependency:** Works with or without protection plugins
- **Configurable:** Allow/deny per plugin, admin claims toggle
- **All entities affected:** Gifts, Wichtel, Elves, Snowmen, Decorations

### 🔧 Performance Features
- **SQLite Snapshots:** Compressed biome storage (~5-10 MB instead of 156 MB)
- **Unlimited Chunks:** No more 2000-chunk limit
- **Budget System:** Max chunks per tick configurable (`perTickBudget`)
- **Multi-Threading Ready:** Folia support with regionalized threading

### 💾 Backup & Safety (NEW in v2.2!)
- **Automatic Backups:** SAFE-Backup before `/xmas on`, Timestamp-Backup after `/xmas off`
- **Emergency Protection:** Backup created if server stops during active event
- **Backup Rotation:** Keeps last 5 backups, stored outside plugins folder
- **Biome Comparison:** Compare current world with any backup to find differences
- **Smart Recovery:** Restore only changed chunks from backup
- **Survives Plugin Deletion:** Backups stored in `world/christmas_backups/`

### 🔔 Update System (NEW in v2.2!)
- **Automatic Checks:** Updates checked on server start via Modrinth API
- **OP Notifications:** Admins get notified on join about new versions
- **Manual Checks:** `/xmas update check` for on-demand updates
- **Smart Versioning:** Semantic version comparison (2.1.0 vs 2.2.0)
- **Multiple Sources:** Modrinth primary, GitHub Releases fallback

---

## 🚀 Multi-Platform Support

**One JAR works on all platforms!**

| Platform | Status | Scheduler Type | Performance |
|----------|--------|----------------|-------------|
| **Paper** | ✅ Tested | Global Timer | 18-20 TPS |
| **Folia** | ✅ Tested | Player-based Entity Scheduler | Optimal for 50+ players |
| **Purpur** | ✅ Compatible | Global Timer | 18-20 TPS |
| **Spigot** | ✅ Compatible | Global Timer | 18-20 TPS |

**Automatic Detection:** The plugin detects the platform on startup and chooses the optimal strategy!

---

## 📦 Installation

1. **Download:** Get `ChristmasSeason-2.2.0.jar` from [Releases](https://github.com/BoondockSulfur/ChristmasSeason/releases)
2. **Installation:** Copy the JAR to the `plugins/` folder
3. **Server Start:** Start your server (Spigot/Paper/Purpur/Folia)
4. **Configuration:** Adjust `config.yml` (optional)
5. **Activation:** `/xmas on` - Done! 🎄

**Requirements:**
- Minecraft 1.21+ (or Folia 1.20+)
- Java 21+
- Spigot/Paper/Purpur/Folia Server

**Optional (Soft Dependencies):**
- WorldGuard 7.0+ (region protection)
- GriefPrevention (claim protection)

**Downloads:**
- 🎯 **Modrinth:** [christmas-season](https://modrinth.com/plugin/christmas-season)
- 📦 **GitHub:** [Releases](https://github.com/BoondockSulfur/ChristmasSeason/releases)

---

## 🎮 Commands

### Main Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/xmas on` | Activates ChristmasSeason | `christmas.admin` |
| `/xmas off` | Deactivates and restores biomes | `christmas.admin` |
| `/xmas status` | Shows status (active/inactive) | `christmas.admin` |
| `/xmas reload` | Reloads configuration | `christmas.admin` |

### Biome Commands
| Command | Description | Permission |
|---------|-------------|------------|
| `/xmas biome set <biome> [radius]` | Manually sets biomes (only when active) | `christmas.admin` |
| `/xmas biome status` | Shows snapshot database statistics | `christmas.admin` |
| `/xmas biome clearsnap` | Deletes biome snapshot database | `christmas.admin` |
| `/xmas biome compare <backup-ID>` | Compares current biomes with backup | `christmas.admin` |
| `/xmas biome fix-diff <backup-ID> confirm` | Restores differences from backup | `christmas.admin` |

### Backup Commands (NEW in v2.2!)
| Command | Description | Permission |
|---------|-------------|------------|
| `/xmas backup list` | Shows all available backups | `christmas.admin` |
| `/xmas backup restore <ID> confirm` | Restores from backup | `christmas.admin` |
| `/xmas backup create` | Creates manual backup | `christmas.admin` |
| `/xmas backup clear` | Deletes old timestamp backups | `christmas.admin` |

### Update Commands (NEW in v2.2!)
| Command | Description | Permission |
|---------|-------------|------------|
| `/xmas update check` | Checks for new plugin versions | `christmas.admin` |

**Examples:**
```bash
# Basic Usage
/xmas on                          # Starts winter wonderland
/xmas biome set snowy_plains 2    # Changes 5x5 chunks to snowy plains
/xmas off                         # Restores everything back

# Backup Management (NEW!)
/xmas backup list                 # Show all backups
/xmas backup restore 1 confirm    # Restore first backup
/xmas backup create               # Manual backup

# Biome Comparison (NEW!)
/xmas biome compare SAFE          # Compare with SAFE backup
/xmas biome fix-diff SAFE confirm # Fix differences

# Updates (NEW!)
/xmas update check                # Check for new versions
```

---

## ⚙️ Configuration

### Default Settings (recommended):

```yaml
active: false
snowWorld: "world"
language: "de"  # de or en

biome:
  enabled: true
  target: "SNOWY_PLAINS"
  enableSnapshot: true      # Important for restore!

  playerBubble:
    enabled: true
    radiusChunks: 2         # 5x5 chunks around player
    refreshClient: true     # Immediate client updates
    tickIntervalTicks: 40   # Every 2 seconds
    perTickBudget: 12       # 12 chunks per tick (fast!)

  restore:
    perTick: 4              # 4 chunks per tick during /xmas off

snowstorm:
  enabled: true
  mode: auto                # auto, manual, none
  auto:
    onSeconds: 150
    offSeconds: 45

decoration:
  enabled: true
  intervalSeconds: 35
  spawnChance: 0.35
  lifetimeSeconds: 180
  glow: true

gifts:
  enabled: true
  globalIntervalSeconds: 160
  chancePerInterval: 0.35
  lifetimeSeconds: 300
  broadcastOnSpawn: true

wichtel:
  enabled: true
  spawnIntervalSeconds: 45
  maxPerWorld: 6

elves:
  enabled: true
  spawnIntervalSeconds: 60
  maxPerWorld: 4

regionIntegration:
  enabled: true
  worldGuard:
    allowInProtected: false   # Allow spawns in WG regions
  griefPrevention:
    allowInClaims: false      # Allow spawns in claims
    allowInAdminClaims: false # Allow spawns in admin claims

snowmen:
  enabled: true
  spawnIntervalSeconds: 45
  maxPerWorld: 6
  attackChance: 0.15
```

---

## 🔧 Performance Tuning

### For Paper/Spigot/Purpur:

**Standard (recommended):**
- `perTickBudget: 12` - Fast updates without lag
- `tickIntervalTicks: 40` - Every 2 seconds

**Weak Servers:**
- `perTickBudget: 6` - Safer with TPS issues
- `tickIntervalTicks: 60` - Every 3 seconds

**Strong Servers:**
- `perTickBudget: 25` - Instant updates
- `tickIntervalTicks: 20` - Every second

### For Folia:

**Recommended (parallel threads!):**
- `perTickBudget: 25` - All chunks instantly
- `radiusChunks: 3` - Larger radius (7x7)
- `tickIntervalTicks: 20` - Very frequent updates

**Why?** Folia uses regionalized threads - each player gets their own timer!

---

## 📊 Performance Benchmarks

**Tested on Paper (1422 chunks):**
- **Biome Restore:** 17.8 seconds (99.86% success rate)
- **TPS:** Stable 18-20 TPS during `/xmas on`
- **Chunk Processing:** 12 chunks/tick = ~2 ticks (0.1s) for player bubble

**Folia Advantages:**
- Parallel chunk processing across regions
- No main thread blocking
- Better for 50+ players

---

## 🐛 Troubleshooting

### Problem: `NoSuchMethodError: teleportAsync` (Spigot)
**Cause:** Old plugin version before v2.0.0
**Solution:** Update to ChristmasSeason v2.1.0+ (Multi-Platform Support)

### Problem: `UnsupportedOperationException: Must use teleportAsync` (Folia)
**Cause:** Old plugin version before v2.0.0
**Solution:** Update to ChristmasSeason v2.1.0+ (Multi-Platform Support)

### Problem: `IllegalStateException` crashes during `/xmas off` on Folia
**Cause:** Thread safety violations in old versions
**Solution:** Update to ChristmasSeason v2.1.0+ (Critical Folia fixes included)

### Problem: TPS drops on Paper
**Solution:** Reduce `perTickBudget` to 6 or increase `tickIntervalTicks` to 60

### Problem: Biomes don't change
**Solution:**
1. Check `/xmas status` - is `active: true`?
2. Check `snowWorld: "world"` in config.yml
3. Are you in the correct world?

### Problem: Chunk stripes after `/xmas off`
**Solution:**
1. `/xmas on` - Reactivate ChristmasSeason
2. `/xmas biome set snowy_plains 2` - Repair manually
3. `/xmas off` - Restore (now with manual fixes)

### Problem: `/xmas biome set` doesn't work
**Solution:** The command only works when ChristmasSeason is active (`/xmas on`)

### Problem: Lost biome data after plugin deletion
**Solution (NEW in v2.2!):**
1. Check `world/christmas_backups/` for automatic backups
2. `/xmas backup list` - List available backups
3. `/xmas backup restore SAFE confirm` - Restore SAFE backup
4. Alternative: `/xmas biome compare SAFE` to see what changed

### Problem: Schneemänner spawning in water
**Solution:** Fixed in v2.2.0! Update to latest version.

### Problem: Wichtel/Schneemänner spawning on roofs or in trees
**Solution:** Fixed in v2.2.0! Update to latest version.

---

## 🔄 Migration Guide

### From v2.1.0 to v2.2.0

**Highly recommended for all servers!** This update fixes critical bugs, adds region protection, and backup system.

1. **Stop the server**
2. **Replace the JAR** with `ChristmasSeason-2.2.0.jar`
3. **Start the server** - done!

**What's New:**
- ✅ Config compatible (new `regionIntegration` section added automatically)
- ✅ Database fully compatible
- ✅ **NEW:** WorldGuard & GriefPrevention support (auto-detected)
- ✅ **NEW:** Automatic backup system (SAFE/Timestamp/Emergency)
- ✅ **NEW:** Update notifications for OPs
- ✅ **NEW:** Biome comparison & recovery tools
- ✅ **NEW:** Full tab completion for all commands
- ✅ **NEW:** bStats metrics
- ✅ **FIXED:** 17 bugs including 3 critical (backup system, Folia crashes, thread-safety)
- ✅ **NEW:** 7 data loss protection measures

**Optional:** Install WorldGuard or GriefPrevention for region protection.

**First Start:**
- Plugin will check for updates automatically
- DB integrity check runs on startup
- SAFE-Backup will be created on first `/xmas on`
- Backups stored in `world/christmas_backups/` (survives plugin deletion!)

### From v2.0.0 to v2.1.0

**Recommended for all Folia servers!** This update fixes critical thread-safety issues.

1. **Stop the server**
2. **Replace the JAR** with `ChristmasSeason-2.1.0.jar`
3. **Start the server** - done!

**Changes:**
- ✅ Config remains identical
- ✅ Database fully compatible
- ✅ Critical Folia fixes (no more crashes during `/xmas off`)
- ✅ Complete internationalization (all logs respect `language` setting)
- ✅ Fixed TPS drops on Folia with `perTickBudget` enforcement

### From v1.4.1

**Good news:** Version 2.1 is fully compatible!

1. **Stop the server**
2. **Replace the old JAR** with `ChristmasSeason-2.1.0.jar`
3. **Start the server** - done!

**Changes:**
- ✅ Config remains identical (optional: add `perTickBudget: 12`)
- ✅ Database compatible (SQLite automatically migrated)
- ✅ Same performance on Paper as v1.4.1
- ✅ Bonus: Now works on Folia too!

---

## 📝 Changelog

See [CHANGELOG.md](CHANGELOG.md) for detailed version history.

**v2.2.0 Highlights (Latest):**
- 🛡️ **NEW:** WorldGuard & GriefPrevention Integration (no spawns in protected areas)
- 💾 **NEW:** Automatic Backup System (SAFE/Timestamp/Emergency backups)
- 🔔 **NEW:** Update Checker with Modrinth/GitHub API integration
- 🔍 **NEW:** Biome Comparison & Recovery Tools
- ⌨️ **NEW:** Full Tab Completion for all commands
- 📊 **NEW:** bStats Metrics (Plugin ID: 30930)
- 🔒 **NEW:** 7 Enhanced Data Loss Protection measures
- 🐛 **FIXED:** 17 bugs including 3 critical (backup system, Folia crashes, thread-safety)

**v2.1.0 Highlights:**
- 🔥 **CRITICAL:** Fixed Folia crashes during `/xmas off` (thread-safety violations)
- 🔥 **CRITICAL:** Fixed TPS drops to 16 on Folia (proper `perTickBudget` enforcement)
- 🔥 **CRITICAL:** Fixed `/xmas biome set` not creating snapshots correctly
- 🌐 Complete internationalization (all 68+ log messages now respect `language` setting)
- 🔄 Automatic chunk retry mechanism (fixes "missing chunks" issue)
- ✅ Fixed client-side biome caching after `/xmas off`

**v2.0.0 Highlights:**
- 🎄 Multi-Platform Support (Spigot/Paper/Purpur/Folia)
- ⚡ Chunk Queue System (prevents TPS spikes)
- 🐛 Race Condition Fixes (biome set, restore)
- 🚀 2x faster biome updates (perTickBudget: 12)
- 🔧 Improved cache management

---

## 📜 License

**MIT License**

Copyright (c) 2025 Boondock_Sulfur

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

---

## 🎯 Developer Notes

### Dependencies:
- **Paper API** 1.21.3-R0.1-SNAPSHOT
- **FoliaLib** 0.4.3 (shaded & relocated)
- **SQLite JDBC** 3.45.0.0 (shaded)
- **bStats** 3.1.0 (shaded & relocated)
- **WorldGuard API** 7.0.9 (provided, optional)
- **GriefPrevention** (reflection-based, no dependency needed)

### Build:
```bash
mvn clean package
```

### Scheduler Usage:
```java
FoliaSchedulerHelper scheduler = new FoliaSchedulerHelper(plugin);

// Global Task (Weather, etc.)
scheduler.runGlobalTask(() -> { ... });

// Location-based Task (Chunks, Blocks)
scheduler.runAtLocation(location, () -> { ... });

// Entity-based Task (Mobs, Items)
scheduler.runForEntity(entity, () -> { ... });
```

---

## 🔧 Configuration Notes

### Update Checker Configuration

The plugin automatically checks for updates on Modrinth. If you've published the plugin on a different platform, you can configure the URLs in `UpdateChecker.java`:

```java
// UpdateChecker.java (lines 24-25)
private static final String MODRINTH_SLUG = "christmas-season";
private static final String GITHUB_REPO = "BoondockSulfur/ChristmasSeason";
```

Update notifications will appear:
- In console on server start
- For OPs when they join the server
- On demand with `/xmas update check`

---

**Have fun with ChristmasSeason v2.2! 🎄❄️**

**Support & Links:**
- 🐛 **Issues:** [GitHub Issues](https://github.com/BoondockSulfur/ChristmasSeason/issues)
- 💬 **Discussions:** [GitHub Discussions](https://github.com/BoondockSulfur/ChristmasSeason/discussions)
- 📥 **Downloads:** [Modrinth](https://modrinth.com/plugin/christmas-season) | [GitHub Releases](https://github.com/BoondockSulfur/ChristmasSeason/releases)
