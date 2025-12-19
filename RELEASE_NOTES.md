# ChristmasSeason v2.0.0 - Multi-Platform Edition 🎄

**Release Date:** 2025-12-19

---

## 🎉 Major Release: Full Multi-Platform Support!

ChristmasSeason v2.0.0 is a **major release** that brings full compatibility with **Spigot, Paper, Purpur, and Folia** - all from a single JAR file!

This release includes critical performance improvements, bug fixes, and a complete internal rewrite to support Folia's regionalized threading model while maintaining perfect backward compatibility with traditional server platforms.

---

## ✨ Highlights

### 🎄 **Multi-Platform Support**
- **One JAR, Four Platforms:** Single plugin file works on Spigot, Paper, Purpur, AND Folia
- **Automatic Detection:** Platform is detected at runtime - zero configuration needed
- **Smart Scheduler Strategy:**
  - Paper/Spigot/Purpur: Global timer (preserves v1.4.1 performance - 18-20 TPS)
  - Folia: Player-based Entity Scheduler (regionalized threading for 50+ players)

### ⚡ **Performance Improvements**
- **2x Faster Biome Updates:** `perTickBudget` increased from 6 → 12 chunks/tick
- **Chunk Queue System:** Budget-based processing prevents TPS spikes when players move quickly
- **Optimized Caching:** Improved cache management for `processedChunks` and `knownSnapshotChunks`

### 🛠️ **New Features**
- **Manual Biome Correction:** `/xmas biome set <biome> [radius]` command for fixing biome issues
- **FoliaLib Integration:** Seamless multi-platform scheduler abstraction (v0.4.3)
- **Enhanced Error Handling:** Better logging and recovery for chunk loading failures

---

## 🐛 Critical Fixes

### **Multi-Platform Teleport Incompatibility**
- Fixed `NoSuchMethodError` on Spigot (teleportAsync doesn't exist)
- Fixed `UnsupportedOperationException` on Folia (must use teleportAsync)
- Wichtel can now teleport correctly on all platforms

### **Race Condition in `/xmas biome set`**
- Fixed global timer overwriting manual biome changes
- Chunks are now marked as processed BEFORE async tasks start
- Manual changes persist until `/xmas off`

### **Chunk Stripes Not Restored**
- Fixed chunks being deleted from database even when restore failed
- Chunks only deleted after successful restoration
- Failed chunks remain in DB for retry on next `/xmas off`

### **Database Management**
- Improved cache clearing before/after restore operations
- Better error handling for chunk loading failures
- Success-only database cleanup prevents data loss

---

## 📊 Performance Benchmarks

**Tested on Paper (1422 chunks):**
- **Biome Restore:** 17.8 seconds (99.86% success rate)
- **TPS:** Stable 18-20 TPS during active season
- **Chunk Processing:** 12 chunks/tick = ~0.1s for 25-chunk player bubble

**Tested on Folia (390 chunks):**
- **Biome Restore:** 4.9 seconds (99.5% success rate)
- **TPS:** Optimal for 50+ players (regionalized threading)
- **Chunk Processing:** Parallel processing across regions

---

## 🔄 Migration from v1.4.1

**Good news:** v2.0.0 is fully backward compatible!

### Steps:
1. Stop your server
2. Replace old JAR with `ChristmasSeason-2.0.0.jar`
3. Start your server - done!

### What's preserved:
- ✅ Config files (no changes needed)
- ✅ Database format (SQLite automatically migrated)
- ✅ Same performance on Paper/Spigot/Purpur as v1.4.1
- ✅ Bonus: Now works on Folia too!

### Optional config enhancement:
```yaml
biome:
  playerBubble:
    perTickBudget: 12  # New default (was 6) - 2x faster updates!
```

---

## 📦 Installation

### Requirements:
- **Minecraft:** 1.21+ (or Folia 1.20+)
- **Java:** 21+
- **Server:** Spigot, Paper, Purpur, or Folia

### Installation Steps:
1. Download `ChristmasSeason-2.0.0.jar`
2. Place in `plugins/` folder
3. Start server
4. (Optional) Configure `config.yml`
5. Run `/xmas on` - Enjoy! 🎄

---

## 🎮 Commands

| Command | Description |
|---------|-------------|
| `/xmas on` | Activate ChristmasSeason |
| `/xmas off` | Deactivate and restore biomes |
| `/xmas status` | Show current status |
| `/xmas reload` | Reload configuration |
| `/xmas biome set <biome> [radius]` | Manually set biomes (NEW!) |
| `/xmas biome clearsnap` | Delete biome snapshot database |

---

## 🔧 Technical Changes

### Dependencies:
- **Paper API:** 1.21.3-R0.1-SNAPSHOT (upgraded from Spigot API)
- **FoliaLib:** 0.4.3 (shaded & relocated)
- **SQLite JDBC:** 3.45.0.0 (bundled)

### Scheduler Migration:
- All managers migrated from Bukkit scheduler to FoliaLib
- Created `FoliaSchedulerHelper` for platform-agnostic scheduling
- Replaced `BukkitTask` with `WrappedTask` (FoliaLib wrapper)
- Replaced `BukkitRunnable` with lambda expressions

### Build Configuration:
- Maven Shade plugin updated to include FoliaLib
- Relocation: `com.tcoded.folialib` → `de.boondocksulfur.christmas.libs.folialib`
- Added tcoded-releases repository

---

## 📋 Full Changelog

For detailed technical changes, see [CHANGELOG.md](CHANGELOG.md)

---

## 🙏 Acknowledgments

- **Paper Team:** For the excellent Paper API
- **Folia Team:** For regionalized threading innovation
- **TCoded:** For FoliaLib (multi-platform scheduler abstraction)
- **Community:** For testing and feedback during development

---

## 🐛 Known Issues

None at this time! All critical bugs from testing have been resolved.

If you encounter any issues, please report them on the GitHub Issues page.

---

## 📄 License

**MIT License** - See LICENSE file for details.

Copyright (c) 2025 Boondock_Sulfur

---

**Happy Holidays! 🎄❄️**

May your server be merry and your TPS stay high!
