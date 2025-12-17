package com.github.tejaslamba2006.auditcord.commands;

import com.github.tejaslamba2006.auditcord.AuditCord;
import com.github.tejaslamba2006.auditcord.data.PlayerInfo;
import com.github.tejaslamba2006.auditcord.utils.AuditLogger;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AuditCordCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("reload", "info", "list", "stats", "help");
    private final AuditCord plugin;

    public AuditCordCommand(AuditCord plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("auditcord.admin")) {
            AuditLogger.sendError(sender, "You don't have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> handleReload(sender);
            case "info" -> {
                if (args.length < 2) {
                    AuditLogger.sendError(sender, "Usage: /auditcord info <player>");
                    return true;
                }
                handleInfo(sender, args[1]);
            }
            case "list" -> handleList(sender);
            case "stats" -> handleStats(sender);
            default -> sendHelpMessage(sender);
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        try {
            plugin.reloadPlugin();
            AuditLogger.sendSuccess(sender, "AuditCord configuration has been reloaded successfully!");
        } catch (Exception e) {
            AuditLogger.sendError(sender, "Failed to reload configuration: " + e.getMessage());
        }
    }

    private void handleInfo(CommandSender sender, String playerName) {
        PlayerInfo playerInfo = plugin.getPlayerInfoManager().getPlayerInfo(playerName);

        if (playerInfo == null) {
            AuditLogger.sendError(sender, "No information found for player: " + playerName);
            return;
        }

        AuditLogger.sendMessage(sender, "§6=== Player Information: " + playerInfo.getPlayerName() + " ===");
        AuditLogger.sendMessage(sender, "§7UUID: §f" + playerInfo.getUuid());
        AuditLogger.sendMessage(sender, "§7Client: §f" + playerInfo.getFormattedClientInfo());
        AuditLogger.sendMessage(sender,
                "§7Mod Loader: §f" + (playerInfo.getModLoader() != null ? playerInfo.getModLoader() : "Unknown"));

        if (playerInfo.isCustomClient()) {
            AuditLogger.sendMessage(sender, "§7Custom Client: §a" + playerInfo.getCustomClientName());
        }

        if (!playerInfo.getModList().isEmpty()) {
            AuditLogger.sendMessage(sender,
                    "§7Mods (" + playerInfo.getModList().size() + "): §f" + playerInfo.getFormattedModList());
        } else {
            AuditLogger.sendMessage(sender, "§7Mods: §7No mods detected");
        }

        AuditLogger.sendMessage(sender, "§7IP Address: §f" + playerInfo.getIpAddress());
        AuditLogger.sendMessage(sender, "§7Ping: §f" + playerInfo.getPing() + "ms");

        if (playerInfo.getCountry() != null) {
            AuditLogger.sendMessage(sender,
                    "§7Country: §f" + playerInfo.getCountryFlag() + " " + playerInfo.getCountry());
        }

        if (playerInfo.getOperatingSystem() != null) {
            AuditLogger.sendMessage(sender, "§7OS: §f" + playerInfo.getOperatingSystem());
        }

        AuditLogger.sendMessage(sender, "§7First Join: §f" + playerInfo.getFirstJoin());
        AuditLogger.sendMessage(sender, "§7Last Seen: §f" + playerInfo.getLastSeen());

        if (playerInfo.isStaff()) {
            AuditLogger.sendMessage(sender, "§7Status: §9Staff Member");
        }

        if (playerInfo.isSuspicious()) {
            AuditLogger.sendMessage(sender, "§7Status: §cSuspicious Activity Detected");
        }
    }

    private void handleList(CommandSender sender) {
        Map<UUID, PlayerInfo> allPlayers = plugin.getPlayerInfoManager().getAllPlayerInfo();

        if (allPlayers.isEmpty()) {
            AuditLogger.sendMessage(sender, "No player information available.");
            return;
        }

        AuditLogger.sendMessage(sender, "§6=== Online Players with Client Information ===");

        for (PlayerInfo info : allPlayers.values()) {
            Player player = Bukkit.getPlayer(info.getPlayerName());
            if (player != null && player.isOnline()) {
                StringBuilder status = new StringBuilder();
                if (info.isStaff())
                    status.append("§9[STAFF] ");
                if (info.isSuspicious())
                    status.append("§c[SUSPICIOUS] ");

                AuditLogger.sendMessage(sender,
                        status + "§f" + info.getPlayerName() + " §7- §f" + info.getFormattedClientInfo());
            }
        }
    }

    private void handleStats(CommandSender sender) {
        var manager = plugin.getPlayerInfoManager();
        Map<UUID, PlayerInfo> allPlayers = manager.getAllPlayerInfo();

        int total = allPlayers.size();
        int staff = 0, suspicious = 0, lunar = 0, badlion = 0, labymod = 0, forge = 0, fabric = 0;

        for (PlayerInfo p : allPlayers.values()) {
            if (p.isStaff())
                staff++;
            if (p.isSuspicious())
                suspicious++;
            String custom = p.getCustomClientName();
            if (custom != null) {
                if (custom.contains("Lunar"))
                    lunar++;
                else if (custom.contains("Badlion"))
                    badlion++;
                else if (custom.contains("LabyMod"))
                    labymod++;
            }
            if (p.hasForge())
                forge++;
            if (p.hasFabric())
                fabric++;
        }

        AuditLogger.sendMessage(sender, "§6=== AuditCord Statistics ===");
        AuditLogger.sendMessage(sender, "§7Total Players Tracked: §f" + total);
        AuditLogger.sendMessage(sender, "§7Staff Members: §9" + staff);
        AuditLogger.sendMessage(sender, "§7Suspicious Players: §c" + suspicious);
        AuditLogger.sendMessage(sender, "§7Lunar Client: §f" + lunar);
        AuditLogger.sendMessage(sender, "§7Badlion Client: §f" + badlion);
        AuditLogger.sendMessage(sender, "§7LabyMod: §f" + labymod);
        AuditLogger.sendMessage(sender, "§7Forge: §f" + forge);
        AuditLogger.sendMessage(sender, "§7Fabric/Quilt: §f" + fabric);
        AuditLogger.sendMessage(sender, "§7Debug Mode: " + (plugin.isDebugEnabled() ? "§aEnabled" : "§cDisabled"));
        AuditLogger.sendMessage(sender,
                "§7Discord Logging: " + (plugin.isDiscordLoggingEnabled() ? "§aEnabled" : "§cDisabled"));
    }

    private void sendHelpMessage(CommandSender sender) {
        AuditLogger.sendMessage(sender, "§6=== AuditCord Commands ===");
        AuditLogger.sendMessage(sender, "§7/auditcord reload §f- Reload plugin configuration");
        AuditLogger.sendMessage(sender, "§7/auditcord info <player> §f- Show detailed player information");
        AuditLogger.sendMessage(sender, "§7/auditcord list §f- List online players with client info");
        AuditLogger.sendMessage(sender, "§7/auditcord stats §f- Show plugin statistics");
        AuditLogger.sendMessage(sender, "§7/auditcord help §f- Show this help message");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("auditcord.admin"))
            return List.of();

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> result = new ArrayList<>();
            for (String cmd : SUBCOMMANDS) {
                if (cmd.startsWith(input))
                    result.add(cmd);
            }
            return result;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
            String input = args[1].toLowerCase();
            List<String> result = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(input))
                    result.add(p.getName());
            }
            return result;
        }

        return List.of();
    }
}