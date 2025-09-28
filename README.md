# MC RankUp

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21-green.svg)](https://papermc.io/downloads/paper)
[![PaperMC](https://img.shields.io/badge/PaperMC-Supported-blue.svg)](https://papermc.io)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

An advanced rankup plugin for **PaperMC 1.21** that rewards players for mining blocks. Ranks increase progressively with an exponential requirement curve, making progression challenging and rewarding. Optimized for servers with asynchronous operations, batching, and SQLite storage. Includes a dynamic progress bar in the actionbar and optional PlaceholderAPI integration.

> **Why this plugin?** Traditional rankups can feel grindy or linear. This one scales dynamically—start easy, but higher ranks demand strategy and teamwork. Perfect for survival servers focused on mining economies!

## ✨ Features

- **Mining-Based Progression**: Earn ranks by breaking blocks (any type, customizable via events).
- **Exponential Scaling**: Required blocks grow via a configurable multiplier (e.g., base 100 * 1.5^rank).
- **Configurable Max Rank**: Set a cap (e.g., 100) to prevent infinite grinding.
- **Real-Time Progress Bar**: Visual actionbar shows a colored progress bar (`:` filled green, `:` empty red) with percentage on every mine (throttled for performance).
- **SQLite Database**: Persistent storage with WAL mode, async batching, and weak caching for high concurrency.
- **Global Broadcasts**: Announce rankups server-wide (toggleable).
- **PlaceholderAPI Support**: Use `%mcrankup_rank%`, `%mcrankup_blocks%`, `%mcrankup_progress%` in other plugins (e.g., chat, scoreboards).
- **Optimizations**: Batch saves (every 10 breaks), async flushes (every 5s), throttled actionbars (every 1s). All configurable.
- **Extensible**: Modular code (events, utils) for easy customization.

## 📦 Installation

1. **Download**: Grab the latest JAR from [Releases](https://github.com/yourusername/rankup-avanzado/releases) or build from source (see below).
2. **Server Setup**: Ensure you're running **PaperMC 1.21** (or compatible Spigot fork).
3. **Place the JAR**: Drop `MCRankup-1.0.0.jar` into your `plugins/` folder.
4. **Restart**: Start/restart your server. The plugin will auto-generate `config.yml` and `playerdata.db`.
5. **Optional**: Install [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) for placeholders.

> **Dependencies**: None required (PAPI is soft-depend). SQLite is bundled.

## ⚙️ Configuration

Edit `plugins/Advanced RankUp/config.yml` for customization. Here's a sample:

```yaml
# Advanced RankUp Plugin Configuration

# Maximum rank that can be reached
max-rank: 100

# Base blocks required for the first rankup (from rank 1 to 2)
base-blocks: 100

# Multiplier for exponential increase (1.5 = 1.5x more each rank)
multiplier: 1.5

# Announce rankup in global chat?
broadcast-rankup: true

# Optimization settings for high-player servers
optimizations:
  # Batch size for pending block increments (save every N breaks)
  batch-size: 10
  # Flush interval in ticks (20 ticks = 1 second; 0 to disable)
  flush-interval-ticks: 100  # Every 5 seconds
  # Actionbar throttle delay in milliseconds (0 = every break)
  actionbar-throttle-ms: 1000  # Every 1 second

# Messages (supports & color codes)
messages:
  rankup-success: "&aCongratulations! You have ranked up to %rank%."
  cannot-rankup: "&cYou cannot rank up further. Maximum rank: %maxrank%."
  progress-actionbar: "&8Progress: [&a%bar%&8] (&a%percent%%&8)"  # %bar% = progress bar, %percent% = %

# PlaceholderAPI integration
placeholderapi:
  enabled: true
```