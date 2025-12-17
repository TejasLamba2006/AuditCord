# AuditCord

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.0-blue)
![Minecraft](https://img.shields.io/badge/Minecraft-1.19+-green)
![Java](https://img.shields.io/badge/Java-21+-orange)
![License](https://img.shields.io/badge/license-Source--Available-red)

**A lightweight, high-performance client brand detection plugin for Spigot/Paper servers**

*🚀 No dependencies required • ⚡ Fast & Efficient • 💻 Perfect for low-end servers*

</div>

---

## ✨ Why AuditCord?

Unlike other client detection plugins that require **ProtocolLib** or other heavy dependencies, AuditCord uses **native Bukkit reflection** to detect client brands. This means:

| Feature | AuditCord | Other Plugins |
|---------|-----------|---------------|
| Dependencies | **None** ✅ | ProtocolLib required ❌ |
| Resource Usage | **Minimal** ⚡ | Higher overhead |
| Setup Time | **Instant** 🚀 | Extra downloads needed |
| Low-end Server Support | **Excellent** 💪 | May cause lag |
| Startup Time | **Faster** ⏱️ | Dependency loading overhead |

---

## 🎯 Features

- 🚀 **Zero Dependencies** - No ProtocolLib or other plugins required
- ⚡ **Lightweight & Fast** - Minimal resource usage, perfect for low-end servers
- 🔍 **Client Brand Detection** - Detect Vanilla, Forge, Fabric, Lunar Client, BadLion, and more
- 🛡️ **Blacklist/Whitelist System** - Control which clients can join your server
- 🔔 **Staff Alerts** - Notify staff when players join with specific clients
- 📝 **Discord Integration** - Send join logs to Discord via webhooks
- 🎨 **Customizable Messages** - Full control over all plugin messages
- 📊 **Player Statistics** - View online players and their clients
- 🔄 **Brand Replacements** - Customize how client names are displayed

---

## 📥 Installation

1. Download `AuditCord-1.0.0.jar`
2. Place it in your server's `plugins` folder
3. Restart your server
4. Configure in `plugins/AuditCord/config.yml`

**That's it!** No additional dependencies needed. 🎉

---

## 📋 Requirements

| Requirement | Version |
|-------------|---------|
| Server | Spigot/Paper 1.19+ |
| Java | 21 or higher |
| Dependencies | **None!** |

---

## 🎮 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/auditcord` or `/ac` | Main plugin command | `auditcord.admin` |
| `/ac reload` | Reload configuration | `auditcord.admin` |
| `/ac info <player>` | Get player's client info | `auditcord.admin` |
| `/ac list [page]` | List online players & clients | `auditcord.admin` |
| `/ac stats` | View plugin statistics | `auditcord.admin` |
| `/ac help` | Show help menu | `auditcord.admin` |

**Aliases**: `/ac`, `/audit`

---

## 🔑 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `auditcord.admin` | Access to all AuditCord commands | OP |
| `auditcord.notifications` | Receive client detection notifications | OP |
| `auditcord.staff` | Marks player as staff member | false |
| `auditcord.alert` | Receive client brand detection alerts | OP |
| `auditcord.bypassblacklist` | Bypass client brand restrictions | false |

---

## ⚙️ Configuration

```yaml
# Debug mode - Enable detailed logging
debug: false

# Alert settings - Notify staff when players join
alert:
  enabled: true
  message: "&8[&bAuditCord&8] &e%player% &7joined with client: &f%brand%"

# Blacklist - Kick players using these clients
# Use ** as wildcard (e.g., "hacked**" matches "hackedclient", "hackedmod", etc.)
blacklist:
  - "hacked**"
  - "cheat**"
  - "xray**"

# Whitelist mode - Only allow specific clients
use-whitelist: false
whitelist:
  - "vanilla"
  - "fabric"
  - "forge"
  - "lunarclient**"

# Brand name replacements for display
replacements:
  vanilla: "Vanilla Client"
  fabric: "Fabric Modloader"
  forge: "Forge Client"
  lunarclient**: "Lunar Client"
  Geyser: "Bedrock Player!"

# Kick message for blocked clients
kick-message: "&cYour client is not allowed on this server!"

# Discord webhook integration
log-to-discord: false
discord:
  webhook-url: ""
```

### Discord Integration

1. Create a webhook in your Discord server
2. Copy the webhook URL
3. Set `log-to-discord: true`
4. Paste the URL in `discord.webhook-url`

---

## 🔍 Detected Clients

AuditCord can detect and identify:

| Client | Detection |
|--------|-----------|
| Vanilla Minecraft | ✅ |
| Forge (all versions) | ✅ |
| Fabric | ✅ |
| Quilt | ✅ |
| Lunar Client | ✅ |
| BadLion Client | ✅ |
| Feather Client | ✅ |
| LabyMod | ✅ |
| Geyser (Bedrock) | ✅ |
| And many more! | ✅ |

---

## 🔧 How It Works

AuditCord uses **native Bukkit/Spigot reflection** to detect client brands without requiring external dependencies. This approach:

- ✅ **Reduces server startup time** - No dependency loading
- ✅ **Uses less memory** - Smaller plugin footprint
- ✅ **Works everywhere** - Compatible with any Spigot/Paper fork
- ✅ **Simple maintenance** - No dependency updates to manage

---

## 🛠️ Building from Source

```bash
git clone https://github.com/tejaslamba2006/AuditCord.git
cd AuditCord
./gradlew build
```

The compiled JAR will be in `build/libs/AuditCord-1.0.0.jar`

---

## 📜 License

This project is **Source-Available** under a custom license.

### What you CAN do

- ✅ View and study the source code for educational purposes
- ✅ Use the compiled plugin on your servers (personal or commercial)
- ✅ Fork for personal, non-distributed modifications
- ✅ Report bugs and suggest improvements

### What you CANNOT do

- ❌ Copy, reproduce, or redistribute the source code
- ❌ Create derivative works for distribution
- ❌ Sell or commercially distribute this software
- ❌ Remove copyright notices or claim authorship

### Revenue Sharing

If permission is granted to use/distribute this code commercially, a **5% revenue share** applies to all income generated (including ad revenue).

See the [LICENSE](LICENSE) file for full terms.

---

## 💬 Support & Contact

- **Discord**: [Join Server](https://discord.gg/7fQPG4Grwt)
- **Issues**: [GitHub Issues](https://github.com/tejaslamba2006/AuditCord/issues)
- **Source**: [GitHub Repository](https://github.com/tejaslamba2006/AuditCord)
- **Licensing**: Contact via Discord for commercial licensing inquiries

---

## 📝 Changelog

### Version 1.0.0

- 🎉 Initial release
- 🔍 Client brand detection (no dependencies!)
- 🛡️ Blacklist/whitelist system with wildcard support
- 📝 Discord webhook integration
- 🎨 Customizable messages and brand replacements
- 🔔 Staff alert system
- 📊 Player statistics and listing

---

<div align="center">

**Made with ❤️ by [tejaslamba2006](https://github.com/tejaslamba2006)**

⭐ Star this project if you find it useful!

</div>
