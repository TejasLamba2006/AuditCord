package com.github.tejaslamba2006.auditcord.utils;

import com.github.tejaslamba2006.auditcord.AuditCord;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AuditLogger {

    private static volatile AuditCord plugin;
    private static final String PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "AuditCord" + ChatColor.DARK_GRAY
            + "] " + ChatColor.RESET;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static ExecutorService executor;

    public static void initialize(AuditCord auditCord) {
        plugin = auditCord;
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "AuditCord-Discord");
                t.setDaemon(true);
                return t;
            });
        }
    }

    public static void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                executor.shutdownNow();
            }
        }
        plugin = null;
    }

    public static void log(String message) {
        AuditCord p = plugin;
        if (p == null) {
            System.out.println("[AuditCord] " + message);
            return;
        }
        p.getLogger().info(message);
    }

    public static void debug(String message) {
        AuditCord p = plugin;
        if (p == null)
            return;
        if (p.isDebugEnabled()) {
            p.getLogger().info("[DEBUG] " + message);
        }
    }

    public static void logClientDetection(String playerName, String clientInfo) {
        log("Player " + playerName + " is using " + clientInfo);

        AuditCord p = plugin;
        if (p != null && p.isDiscordLoggingEnabled()) {
            Player player = Bukkit.getPlayer(playerName);
            if (player != null && player.isOnline()) {
                logEnhancedClientDetection(player, clientInfo);
            }
        }
    }

    public static void logStaffLogin(String playerName, String clientInfo) {
        log("Staff member " + playerName + " joined using " + clientInfo);

        AuditCord p = plugin;
        if (p != null && p.isDiscordLoggingEnabled()) {
            Player player = Bukkit.getPlayer(playerName);
            if (player != null && player.isOnline()) {
                sendDiscordEmbedAsync(player, clientInfo, "STAFF_ALERT");
            }
        }
    }

    public static void logSuspiciousActivity(String playerName, String reason) {
        log("Suspicious activity detected for " + playerName + ": " + reason);

        AuditCord p = plugin;
        if (p != null && p.isDiscordLoggingEnabled()) {
            Player player = Bukkit.getPlayer(playerName);
            if (player != null && player.isOnline()) {
                sendDiscordEmbedAsync(player, reason, "SUSPICIOUS");
            }
        }
    }

    public static void logEnhancedClientDetection(Player player, String clientInfo) {
        sendDiscordEmbedAsync(player, clientInfo, "CLIENT_DETECTION");
    }

    private static void sendDiscordEmbedAsync(Player player, String info, String eventType) {
        AuditCord p = plugin;
        if (p == null)
            return;

        String webhookUrl = p.getDiscordWebhookUrl();
        if (webhookUrl == null || webhookUrl.isEmpty())
            return;

        ExecutorService exec = executor;
        if (exec == null || exec.isShutdown())
            return;

        String playerName = player.getName();
        String playerUUID = player.getUniqueId().toString();
        String worldName = player.getWorld().getName();
        String worldType = getWorldType(player.getWorld().getEnvironment());
        int ping = player.getPing();
        String playerIP = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "Unknown";
        String location = String.format("X: %.1f, Y: %.1f, Z: %.1f",
                player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
        String gameMode = player.getGameMode().toString();
        String health = String.format("%.1f/20.0", player.getHealth());
        String clientVersion = ClientBrandDetector.getClientVersion(player);
        String serverVersion = Bukkit.getVersion();
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        long timestamp = System.currentTimeMillis() / 1000;

        exec.submit(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(webhookUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "AuditCord/1.0.0");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.setDoOutput(true);

                int embedColor = getEmbedColor(eventType, info);

                String jsonPayload = String.format(
                        "{\"embeds\":[{\"title\":\"AuditCord - Client Detection\"," +
                                "\"description\":\"Player **%s** joined the server\"," +
                                "\"color\":%d,\"timestamp\":\"%s\"," +
                                "\"thumbnail\":{\"url\":\"https://cravatar.eu/avatar/%s/64.png\"}," +
                                "\"fields\":[" +
                                "{\"name\":\"Client Information\",\"value\":\"**Client:** %s\\n**Player:** %s\\n**UUID:** `%s`\\n**Version:** %s\",\"inline\":false},"
                                +
                                "{\"name\":\"Location & World\",\"value\":\"**World:** %s (%s)\\n**Coordinates:** %s\\n**Game Mode:** %s\",\"inline\":true},"
                                +
                                "{\"name\":\"Network & Status\",\"value\":\"**Ping:** %d ms\\n**IP Address:** ||%s||\\n**Health:** %s\",\"inline\":true},"
                                +
                                "{\"name\":\"Server Information\",\"value\":\"**Version:** %s\\n**Online Players:** %d\\n**Timestamp:** <t:%d:F>\",\"inline\":false}"
                                +
                                "],\"footer\":{\"text\":\"AuditCord v1.0.0\"}}]}",
                        escapeJson(playerName), embedColor, LocalDateTime.now().format(TIME_FORMAT), playerUUID,
                        escapeJson(info), escapeJson(playerName), playerUUID, escapeJson(clientVersion),
                        escapeJson(worldName), escapeJson(worldType), escapeJson(location), escapeJson(gameMode),
                        ping, escapeJson(playerIP), escapeJson(health),
                        escapeJson(serverVersion), onlinePlayers, timestamp);

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                }

                connection.getResponseCode();
            } catch (Exception e) {
                AuditCord p2 = plugin;
                if (p2 != null && p2.isDebugEnabled()) {
                    p2.getLogger().warning("Discord webhook failed: " + e.getMessage());
                }
            } finally {
                if (connection != null)
                    connection.disconnect();
            }
        });
    }

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.WHITE + message);
    }

    public static void sendSuccess(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.GREEN + message);
    }

    public static void sendError(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.RED + message);
    }

    public static void sendWarning(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.YELLOW + message);
    }

    private static String getWorldType(org.bukkit.World.Environment environment) {
        return switch (environment) {
            case NORMAL -> "Overworld";
            case NETHER -> "Nether";
            case THE_END -> "The End";
            case CUSTOM -> "Custom";
        };
    }

    private static int getEmbedColor(String eventType, String clientInfo) {
        String lowerClient = clientInfo.toLowerCase();
        if (eventType.equals("SUSPICIOUS") || lowerClient.contains("hack") || lowerClient.contains("cheat")) {
            return 15548997;
        } else if (lowerClient.contains("vanilla")) {
            return 3447003;
        } else if (lowerClient.contains("forge") || lowerClient.contains("fabric")) {
            return 10181046;
        } else if (lowerClient.contains("lunar") || lowerClient.contains("badlion")) {
            return 15158332;
        }
        return 5763719;
    }

    private static String escapeJson(String input) {
        if (input == null)
            return "null";
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}