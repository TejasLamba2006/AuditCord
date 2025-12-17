package com.github.tejaslamba2006.auditcord.data;

import org.bukkit.entity.Player;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerInfoManager {

    private final Map<UUID, PlayerInfo> playerInfoMap = new ConcurrentHashMap<>();

    public PlayerInfo getPlayerInfo(UUID uuid) {
        return playerInfoMap.get(uuid);
    }

    public PlayerInfo getPlayerInfo(String playerName) {
        for (PlayerInfo info : playerInfoMap.values()) {
            if (info.getPlayerName().equalsIgnoreCase(playerName)) {
                return info;
            }
        }
        return null;
    }

    public PlayerInfo getOrCreatePlayerInfo(Player player) {
        UUID uuid = player.getUniqueId();
        PlayerInfo info = playerInfoMap.get(uuid);

        if (info == null) {
            info = new PlayerInfo(player);
            playerInfoMap.put(uuid, info);
        } else {
            info.setPlayerName(player.getName());
            info.setPing(player.getPing());
            info.updateLastSeen();
        }

        return info;
    }

    public void removePlayerInfo(UUID uuid) {
        playerInfoMap.remove(uuid);
    }

    public Map<UUID, PlayerInfo> getAllPlayerInfo() {
        return playerInfoMap;
    }

    public void clearAll() {
        playerInfoMap.clear();
    }

    public int getPlayerCount() {
        return playerInfoMap.size();
    }
}