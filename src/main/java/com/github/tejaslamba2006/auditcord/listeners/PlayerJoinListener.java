package com.github.tejaslamba2006.auditcord.listeners;

import com.github.tejaslamba2006.auditcord.AuditCord;
import com.github.tejaslamba2006.auditcord.data.PlayerInfo;
import com.github.tejaslamba2006.auditcord.utils.AuditLogger;
import com.github.tejaslamba2006.auditcord.utils.ClientBrandDetector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class PlayerJoinListener implements Listener {

    private static final char[] COLOR_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".toCharArray();
    private final AuditCord plugin;

    public PlayerJoinListener(AuditCord plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final UUID playerUuid = event.getPlayer().getUniqueId();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Player player = Bukkit.getPlayer(playerUuid);
            if (player == null || !player.isOnline())
                return;

            String clientBrand = ClientBrandDetector.getClientBrand(player);
            String replacementBrand = plugin.getReplacementBrand(clientBrand);

            PlayerInfo playerInfo = plugin.getPlayerInfoManager().getOrCreatePlayerInfo(player);
            playerInfo.setClientBrand(replacementBrand);

            if (plugin.shouldKickPlayer(player, clientBrand)) {
                String kickMessage = translateColorCodes(plugin.getKickMessage());
                player.kick(Component.text(kickMessage));
                AuditLogger.log("Player " + player.getName() + " kicked for using blocked client: " + clientBrand);
                return;
            }

            if (plugin.getConfig().getBoolean("alert.enabled", true)) {
                String alertMessage = plugin.getConfig().getString("alert.message",
                        "&7[&bAuditCord&7] &e%player% &7joined with client: &f%brand%");
                alertMessage = translateColorCodes(alertMessage
                        .replace("%player%", player.getName())
                        .replace("%brand%", replacementBrand));

                Component alertComponent = Component.text(alertMessage);
                for (Player staff : Bukkit.getOnlinePlayers()) {
                    if (staff.hasPermission("auditcord.alert")) {
                        staff.sendMessage(alertComponent);
                    }
                }
            }

            AuditLogger.logClientDetection(player.getName(), replacementBrand);
        }, 20L);
    }

    private String translateColorCodes(String message) {
        if (message == null || message.isEmpty())
            return message;

        char[] chars = message.toCharArray();
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == '&' && isColorCode(chars[i + 1])) {
                chars[i] = '\u00A7';
                chars[i + 1] = Character.toLowerCase(chars[i + 1]);
            }
        }
        return new String(chars);
    }

    private boolean isColorCode(char c) {
        for (char code : COLOR_CODES) {
            if (c == code)
                return true;
        }
        return false;
    }
}