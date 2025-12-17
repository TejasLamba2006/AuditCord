package com.github.tejaslamba2006.auditcord.utils;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ClientBrandDetector {

    private static volatile String serverVersion;

    private static String getServerVersion() {
        if (serverVersion == null) {
            synchronized (ClientBrandDetector.class) {
                if (serverVersion == null) {
                    try {
                        String packageName = Bukkit.getServer().getClass().getPackage().getName();
                        serverVersion = packageName.replace(".", ",").split(",")[3];
                    } catch (Exception e) {
                        serverVersion = "unknown";
                    }
                }
            }
        }
        return serverVersion;
    }

    public static String getClientBrand(Player player) {
        if (player == null)
            return "Unknown";

        try {
            try {
                String brand = (String) player.getClass().getMethod("getClientBrandName").invoke(player);
                if (brand != null && !brand.isEmpty())
                    return brand;
            } catch (Exception ignored) {
            }

            Object handle = player.getClass().getMethod("getHandle").invoke(player);
            String[] possibleFieldNames = { "clientBrand", "clientBrandName", "brand" };

            for (String fieldName : possibleFieldNames) {
                try {
                    Field brandField = handle.getClass().getDeclaredField(fieldName);
                    brandField.setAccessible(true);
                    Object brandValue = brandField.get(handle);
                    if (brandValue instanceof String s && !s.isEmpty())
                        return s;
                } catch (NoSuchFieldException ignored) {
                }
            }

            try {
                Field connectionField = handle.getClass().getDeclaredField("connection");
                connectionField.setAccessible(true);
                Object connection = connectionField.get(handle);
                Field brandField = connection.getClass().getDeclaredField("clientBrand");
                brandField.setAccessible(true);
                Object brandValue = brandField.get(connection);
                if (brandValue instanceof String s && !s.isEmpty())
                    return s;
            } catch (Exception ignored) {
            }

            try {
                Class<?> craftPlayerClass = Class
                        .forName("org.bukkit.craftbukkit." + getServerVersion() + ".entity.CraftPlayer");
                Object craftPlayer = craftPlayerClass.cast(player);
                Object entityPlayer = craftPlayerClass.getMethod("getHandle").invoke(craftPlayer);

                String[] methods = { "getClientBrand", "getClientBrandName" };
                for (String methodName : methods) {
                    try {
                        Object brand = entityPlayer.getClass().getMethod(methodName).invoke(entityPlayer);
                        if (brand instanceof String s && !s.isEmpty())
                            return s;
                    } catch (Exception ignored) {
                    }
                }

                for (String fieldName : possibleFieldNames) {
                    try {
                        Field brandField = entityPlayer.getClass().getDeclaredField(fieldName);
                        brandField.setAccessible(true);
                        Object brandValue = brandField.get(entityPlayer);
                        if (brandValue instanceof String s && !s.isEmpty())
                            return s;
                    } catch (Exception ignored) {
                    }
                }

            } catch (Exception ignored) {
            }

        } catch (Exception ignored) {
            return "Unknown";
        }

        return "vanilla";
    }

    public static String getClientVersion(Player player) {
        if (player == null)
            return "Unknown";

        try {
            Object handle = player.getClass().getMethod("getHandle").invoke(player);

            try {
                Field connectionField = handle.getClass().getDeclaredField("connection");
                connectionField.setAccessible(true);
                Object connection = connectionField.get(handle);

                Field protocolVersionField = connection.getClass().getDeclaredField("protocolVersion");
                protocolVersionField.setAccessible(true);
                Object protocolVersion = protocolVersionField.get(connection);

                if (protocolVersion instanceof Integer i) {
                    return mapProtocolVersion(i);
                }
            } catch (Exception ignored) {
            }

            try {
                Method getProtocolVersionMethod = player.getClass().getMethod("getProtocolVersion");
                int protocolVersion = (Integer) getProtocolVersionMethod.invoke(player);
                return mapProtocolVersion(protocolVersion);
            } catch (Exception ignored) {
            }

        } catch (Exception ignored) {
        }

        return "Unknown";
    }

    private static String mapProtocolVersion(int protocolVersion) {
        return switch (protocolVersion) {
            case 772 -> "1.21.8";
            case 771 -> "1.21.6";
            case 770 -> "1.21.5";
            case 769 -> "1.21.4";
            case 768 -> "1.21.3";
            case 767 -> "1.21.1";
            case 766 -> "1.20.6";
            case 765 -> "1.20.4";
            case 764 -> "1.20.2";
            case 763 -> "1.20.1";
            case 762 -> "1.19.4";
            case 761 -> "1.19.3";
            case 760 -> "1.19.2";
            case 759 -> "1.19";
            case 758 -> "1.18.2";
            case 757 -> "1.18.1";
            case 756 -> "1.17.1";
            case 755 -> "1.17";
            case 754 -> "1.16.4";
            case 736 -> "1.16.1";
            case 735 -> "1.16";
            case 578 -> "1.15.2";
            case 477 -> "1.14.4";
            case 404 -> "1.13.2";
            case 393 -> "1.13";
            case 340 -> "1.12.2";
            case 315 -> "1.11";
            case 210 -> "1.10.2";
            case 109 -> "1.9.4";
            case 47 -> "1.8.9";
            default -> "Unknown (" + protocolVersion + ")";
        };
    }
}