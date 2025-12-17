package com.github.tejaslamba2006.auditcord.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class PlayerInfo {

    private String playerName;
    private String uuid;
    private String clientBrand;
    private String clientVersion;
    private List<String> modList = new ArrayList<>();
    private String operatingSystem;
    private String modLoader;
    private boolean isCustomClient;
    private String customClientName;
    private int ping;
    private String ipAddress;
    private String country;
    private String countryFlag;
    private LocalDateTime firstJoin;
    private LocalDateTime lastSeen;
    private boolean isStaff;
    private boolean isSuspicious;

    public PlayerInfo(Player player) {
        this.playerName = player.getName();
        this.uuid = player.getUniqueId().toString();
        this.ipAddress = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "Unknown";
        this.ping = player.getPing();
        this.firstJoin = LocalDateTime.now();
        this.lastSeen = LocalDateTime.now();
        this.isStaff = player.hasPermission("auditcord.staff");
        this.modList = new ArrayList<>();
    }

    public void addMod(String modName) {
        if (!this.modList.contains(modName)) {
            this.modList.add(modName);
        }
    }

    public void setCustomClient(String clientName) {
        this.isCustomClient = true;
        this.customClientName = clientName;
    }

    public String getFormattedClientInfo() {
        StringBuilder info = new StringBuilder();

        if (isCustomClient && customClientName != null) {
            info.append(customClientName);
        } else if (clientBrand != null) {
            info.append(clientBrand);
        } else {
            info.append("Unknown Client");
        }

        if (clientVersion != null) {
            info.append(" (").append(clientVersion).append(")");
        }

        if (modLoader != null && !modLoader.equals("Vanilla")) {
            info.append(" [").append(modLoader).append("]");
        }

        return info.toString();
    }

    public String getFormattedModList() {
        if (modList.isEmpty())
            return "No mods detected";
        return String.join(", ", modList);
    }

    public boolean hasForge() {
        return modLoader != null && modLoader.toLowerCase().contains("forge");
    }

    public boolean hasFabric() {
        return modLoader != null
                && (modLoader.toLowerCase().contains("fabric") || modLoader.toLowerCase().contains("quilt"));
    }

    public void updateLastSeen() {
        this.lastSeen = LocalDateTime.now();
    }

    public void detectSuspiciousActivity() {
        if (isCustomClient && modList.size() > 20) {
            this.isSuspicious = true;
            return;
        }

        for (String mod : modList) {
            String modLower = mod.toLowerCase();
            if (modLower.contains("xray") || modLower.contains("hack") ||
                    modLower.contains("cheat") || modLower.contains("autoclicker")) {
                this.isSuspicious = true;
                return;
            }
        }
    }
}