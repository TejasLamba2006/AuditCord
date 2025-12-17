package com.github.tejaslamba2006.auditcord;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import com.github.tejaslamba2006.auditcord.listeners.PlayerJoinListener;
import com.github.tejaslamba2006.auditcord.listeners.PlayerQuitListener;
import com.github.tejaslamba2006.auditcord.commands.AuditCordCommand;
import com.github.tejaslamba2006.auditcord.utils.AuditLogger;
import com.github.tejaslamba2006.auditcord.data.PlayerInfoManager;

import java.util.List;

@Getter
public final class AuditCord extends JavaPlugin {

    private static final String DISCORD_INVITE = "https://discord.gg/7fQPG4Grwt";
    private static AuditCord instance;
    private PlayerInfoManager playerInfoManager;
    private FileConfiguration config;

    private boolean useWhitelist;
    private boolean caseSensitive;
    private boolean debugEnabled;
    private boolean discordLoggingEnabled;
    private String discordWebhookUrl;
    private String kickMessage;
    private List<String> clientList;
    private ConfigurationSection replacements;

    @Override
    public void onEnable() {
        instance = this;

        printStartupBanner();

        loadConfiguration();
        AuditLogger.initialize(this);
        this.playerInfoManager = new PlayerInfoManager();

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);

        AuditCordCommand auditCommand = new AuditCordCommand(this);
        getCommand("auditcord").setExecutor(auditCommand);
        getCommand("auditcord").setTabCompleter(auditCommand);

        getLogger().info("AuditCord has been enabled successfully!");
    }

    private void printStartupBanner() {
        getServer().getConsoleSender().sendMessage("");
        getServer().getConsoleSender().sendMessage("§b     ___   _   _ ____  ___ _____ §3 ____  ___  ____  ____  ");
        getServer().getConsoleSender()
                .sendMessage("§b    / _ \\ | | | |  _ \\|_ _|_   _|§3/ ___|/ _ \\|  _ \\|  _ \\ ");
        getServer().getConsoleSender().sendMessage("§b   | |_| || | | | | | || |  | |  §3| |   | | | | |_) | | | |");
        getServer().getConsoleSender().sendMessage("§b   |  _  || |_| | |_| || |  | |  §3| |___| |_| |  _ <| |_| |");
        getServer().getConsoleSender()
                .sendMessage("§b   |_| |_| \\___/|____/|___| |_|  §3 \\____|\\___/|_| \\_\\____/ ");
        getServer().getConsoleSender().sendMessage("");
        getServer().getConsoleSender().sendMessage("§7    ┌─────────────────────────────────────────────────────┐");
        getServer().getConsoleSender()
                .sendMessage("§7    │  §fAuditCord §8v1.0.0 §7- §bLightweight Client Detection    §7│");
        getServer().getConsoleSender()
                .sendMessage("§7    │  §7Author: §etejaslamba2006                              §7│");
        getServer().getConsoleSender().sendMessage("§7    │  §7Discord: §9" + DISCORD_INVITE + "               §7│");
        getServer().getConsoleSender().sendMessage("§7    └─────────────────────────────────────────────────────┘");
        getServer().getConsoleSender().sendMessage("");
    }

    public void loadConfiguration() {
        saveDefaultConfig();
        reloadConfig();
        this.config = super.getConfig();
        cacheConfigValues();
    }

    private void cacheConfigValues() {
        if (config == null)
            return;

        this.useWhitelist = config.getBoolean("use-whitelist", false);
        this.caseSensitive = config.getBoolean("case-sensitive", false);
        this.debugEnabled = config.getBoolean("debug", false);
        this.discordLoggingEnabled = config.getBoolean("log-to-discord", false);
        this.discordWebhookUrl = config.getString("discord.webhook-url", "");
        this.kickMessage = config.getString("kick-message", "&cYour client is not allowed on this server!");
        this.clientList = config.getStringList(useWhitelist ? "whitelist" : "blacklist");
        this.replacements = config.getConfigurationSection("replacements");
    }

    @Override
    public FileConfiguration getConfig() {
        if (this.config == null)
            loadConfiguration();
        return this.config;
    }

    @Override
    public void onDisable() {
        if (playerInfoManager != null)
            playerInfoManager.clearAll();
        AuditLogger.shutdown();
        getLogger().info("AuditCord has been disabled.");
    }

    public void reloadPlugin() {
        loadConfiguration();
        AuditLogger.initialize(this);
        getLogger().info("AuditCord configuration reloaded.");
    }

    public static AuditCord getInstance() {
        return instance;
    }

    public boolean shouldKickPlayer(org.bukkit.entity.Player player, String clientBrand) {
        if (player.hasPermission("auditcord.bypassblacklist"))
            return false;
        if (clientList == null || clientList.isEmpty())
            return useWhitelist;

        String brandToCheck = caseSensitive ? clientBrand : clientBrand.toLowerCase();

        for (String pattern : clientList) {
            String patternToCheck = caseSensitive ? pattern : pattern.toLowerCase();

            if (patternToCheck.endsWith("**")) {
                if (brandToCheck.startsWith(patternToCheck.substring(0, patternToCheck.length() - 2))) {
                    return !useWhitelist;
                }
            } else if (brandToCheck.equals(patternToCheck)) {
                return !useWhitelist;
            }
        }

        return useWhitelist;
    }

    public String getReplacementBrand(String originalBrand) {
        if (replacements == null)
            return originalBrand;

        String brandToCheck = caseSensitive ? originalBrand : originalBrand.toLowerCase();

        for (String key : replacements.getKeys(false)) {
            String keyToCheck = caseSensitive ? key : key.toLowerCase();

            if (keyToCheck.endsWith("**")) {
                if (brandToCheck.startsWith(keyToCheck.substring(0, keyToCheck.length() - 2))) {
                    return replacements.getString(key);
                }
            } else if (brandToCheck.equals(keyToCheck)) {
                return replacements.getString(key);
            }
        }

        return originalBrand;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public boolean isDiscordLoggingEnabled() {
        return discordLoggingEnabled;
    }

    public String getDiscordWebhookUrl() {
        return discordWebhookUrl;
    }

    public String getKickMessage() {
        return kickMessage;
    }
}
