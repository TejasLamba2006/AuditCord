package com.github.tejaslamba2006.auditcord.listeners;

import com.github.tejaslamba2006.auditcord.AuditCord;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final AuditCord plugin;

    public PlayerQuitListener(AuditCord plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getPlayerInfoManager().removePlayerInfo(event.getPlayer().getUniqueId());
    }
}
