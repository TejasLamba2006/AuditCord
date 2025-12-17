package gg.atomatrix.clientBrand;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ClientBrandPlugin extends JavaPlugin implements Listener {
   private Map<UUID, String> clientBrands = new HashMap();
   private ClientBrandExpansion placeholderExpansion;

   public void onEnable() {
      this.saveDefaultConfig();
      this.getServer().getPluginManager().registerEvents(this, this);
      if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
         this.placeholderExpansion = new ClientBrandExpansion(this);
         this.placeholderExpansion.register();
         this.getLogger().info("PlaceholderAPI integration enabled!");
      }

      this.getLogger().info("ClientBrand plugin enabled!");
   }

   public void onDisable() {
      if (this.placeholderExpansion != null) {
         this.placeholderExpansion.unregister();
      }

      this.getLogger().info("ClientBrand plugin disabled!");
   }

   @EventHandler
   public void onPlayerJoin(PlayerJoinEvent event) {
      final Player player = event.getPlayer();
      (new BukkitRunnable() {
         public void run() {
            String clientBrand = ClientBrandPlugin.this.getClientBrand(player);
            ClientBrandPlugin.this.clientBrands.put(player.getUniqueId(), clientBrand);
            String alertMessage;
            if (ClientBrandPlugin.this.shouldKickPlayer(player, clientBrand)) {
               alertMessage = ClientBrandPlugin.this.getConfig().getString("kick-message", "&cYour client is not allowed on this server!");
               player.kickPlayer(ChatColor.translateAlternateColorCodes('&', alertMessage));
            } else {
               if (ClientBrandPlugin.this.getConfig().getBoolean("alert.enabled", true)) {
                  alertMessage = ClientBrandPlugin.this.getConfig().getString("alert.message", "&7[&bClientBrand&7] &e%player% &7joined with client: &f%brand%");
                  alertMessage = alertMessage.replace("%player%", player.getName()).replace("%brand%", clientBrand);
                  Iterator var3 = Bukkit.getOnlinePlayers().iterator();

                  while(var3.hasNext()) {
                     Player p = (Player)var3.next();
                     if (p.hasPermission("clientbrand.alert")) {
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', alertMessage));
                     }
                  }
               }

            }
         }
      }).runTaskLater(this, 20L);
   }

   private boolean shouldKickPlayer(Player player, String clientBrand) {
      if (player.hasPermission("clientbrand.bypassblacklist")) {
         return false;
      } else {
         boolean useWhitelist = this.getConfig().getBoolean("use-whitelist", false);
         List<String> clientList = this.getConfig().getStringList(useWhitelist ? "whitelist" : "blacklist");
         boolean caseSensitive = this.getConfig().getBoolean("case-sensitive", false);
         String brandToCheck = caseSensitive ? clientBrand : clientBrand.toLowerCase();
         Iterator var7 = clientList.iterator();

         while(var7.hasNext()) {
            String pattern = (String)var7.next();
            String patternToCheck = caseSensitive ? pattern : pattern.toLowerCase();
            if (patternToCheck.endsWith("**")) {
               String prefix = patternToCheck.substring(0, patternToCheck.length() - 2);
               if (brandToCheck.startsWith(prefix)) {
                  return !useWhitelist;
               }
            } else if (brandToCheck.equals(patternToCheck)) {
               return !useWhitelist;
            }
         }

         return useWhitelist;
      }
   }

   private String getClientBrand(Player player) {
      try {
         try {
            String brand = (String)player.getClass().getMethod("getClientBrandName").invoke(player);
            if (brand != null && !brand.isEmpty()) {
               return brand;
            }
         } catch (Exception var19) {
         }

         Object handle = player.getClass().getMethod("getHandle").invoke(player);
         String[] possibleFieldNames = new String[]{"clientBrand", "clientBrandName", "brand"};
         String[] var4 = possibleFieldNames;
         int var5 = possibleFieldNames.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            String fieldName = var4[var6];

            try {
               Field brandField = handle.getClass().getDeclaredField(fieldName);
               brandField.setAccessible(true);
               Object brandValue = brandField.get(handle);
               if (brandValue instanceof String && !((String)brandValue).isEmpty()) {
                  return (String)brandValue;
               }
            } catch (NoSuchFieldException var18) {
            }
         }

         Object entityPlayer;
         try {
            Field connectionField = handle.getClass().getDeclaredField("connection");
            connectionField.setAccessible(true);
            Object connection = connectionField.get(handle);
            Field brandField = connection.getClass().getDeclaredField("clientBrand");
            brandField.setAccessible(true);
            entityPlayer = brandField.get(connection);
            if (entityPlayer instanceof String && !((String)entityPlayer).isEmpty()) {
               return (String)entityPlayer;
            }
         } catch (Exception var17) {
         }

         try {
            String version = this.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);
            entityPlayer = craftPlayerClass.getMethod("getHandle").invoke(craftPlayer);
            String[] methods = new String[]{"getClientBrand", "getClientBrandName"};
            String[] var31 = methods;
            int var10 = methods.length;

            int var11;
            String fieldName;
            for(var11 = 0; var11 < var10; ++var11) {
               fieldName = var31[var11];

               try {
                  Object brand = entityPlayer.getClass().getMethod(fieldName).invoke(entityPlayer);
                  if (brand instanceof String && !((String)brand).isEmpty()) {
                     return (String)brand;
                  }
               } catch (Exception var16) {
               }
            }

            var31 = possibleFieldNames;
            var10 = possibleFieldNames.length;

            for(var11 = 0; var11 < var10; ++var11) {
               fieldName = var31[var11];

               try {
                  Field brandField = entityPlayer.getClass().getDeclaredField(fieldName);
                  brandField.setAccessible(true);
                  Object brandValue = brandField.get(entityPlayer);
                  if (brandValue instanceof String && !((String)brandValue).isEmpty()) {
                     return (String)brandValue;
                  }
               } catch (Exception var15) {
               }
            }
         } catch (Exception var20) {
            this.getLogger().warning("Failed to detect client brand for " + player.getName() + ": " + var20.getMessage());
         }

         return "vanilla";
      } catch (Exception var21) {
         this.getLogger().warning("Error detecting client brand for " + player.getName() + ": " + var21.getMessage());
         return "Unknown";
      }
   }

   public String getPlayerClientBrand(UUID uuid) {
      return (String)this.clientBrands.getOrDefault(uuid, "Unknown");
   }

   public String getReplacementBrand(String originalBrand) {
      ConfigurationSection replacements = this.getConfig().getConfigurationSection("replacements");
      if (replacements != null) {
         boolean caseSensitive = this.getConfig().getBoolean("case-sensitive", false);
         String brandToCheck = caseSensitive ? originalBrand : originalBrand.toLowerCase();
         Iterator var5 = replacements.getKeys(false).iterator();

         while(var5.hasNext()) {
            String key = (String)var5.next();
            String keyToCheck = caseSensitive ? key : key.toLowerCase();
            if (keyToCheck.endsWith("**")) {
               String prefix = keyToCheck.substring(0, keyToCheck.length() - 2);
               if (brandToCheck.startsWith(prefix)) {
                  return replacements.getString(key);
               }
            } else if (brandToCheck.equals(keyToCheck)) {
               return replacements.getString(key);
            }
         }
      }

      return originalBrand;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (command.getName().equalsIgnoreCase("clientbrand")) {
         if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /clientbrand <get|list|reload> [args]");
            return true;
         }

         if (args[0].equalsIgnoreCase("get")) {
            if (!sender.hasPermission("clientbrand.get")) {
               sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
               return true;
            }

            if (args.length < 2) {
               sender.sendMessage(ChatColor.RED + "Usage: /clientbrand get <player>");
               return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
               sender.sendMessage(ChatColor.RED + "Player not found!");
               return true;
            }

            String clientBrand = this.getPlayerClientBrand(target.getUniqueId());
            String message = this.getConfig().getString("get-message", "&7[&bClientBrand&7] &e%player% &7is using: &f%brand%");
            message = message.replace("%player%", target.getName()).replace("%brand%", clientBrand);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            return true;
         }

         if (args[0].equalsIgnoreCase("list")) {
            if (!sender.hasPermission("clientbrand.list")) {
               sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
               return true;
            }

            int page = 1;
            if (args.length > 1) {
               try {
                  page = Integer.parseInt(args[1]);
               } catch (NumberFormatException var16) {
                  sender.sendMessage(ChatColor.RED + "Invalid page number!");
                  return true;
               }
            }

            List<Player> onlinePlayers = new ArrayList(Bukkit.getOnlinePlayers());
            int maxPerPage = 10;
            int totalPages = (int)Math.ceil((double)onlinePlayers.size() / (double)maxPerPage);
            if (page <= totalPages && page >= 1) {
               int startIndex = (page - 1) * maxPerPage;
               int endIndex = Math.min(startIndex + maxPerPage, onlinePlayers.size());
               String headerMessage = this.getConfig().getString("list-header", "&7[&bClientBrand&7] &fPlayer List &7(Page %page%/%total%):");
               headerMessage = headerMessage.replace("%page%", String.valueOf(page)).replace("%total%", String.valueOf(totalPages));
               sender.sendMessage(ChatColor.translateAlternateColorCodes('&', headerMessage));

               for(int i = startIndex; i < endIndex; ++i) {
                  Player p = (Player)onlinePlayers.get(i);
                  String clientBrand = this.getPlayerClientBrand(p.getUniqueId());
                  String listMessage = this.getConfig().getString("list-message", "&7- &e%player%&7: &f%brand%");
                  listMessage = listMessage.replace("%player%", p.getName()).replace("%brand%", clientBrand);
                  sender.sendMessage(ChatColor.translateAlternateColorCodes('&', listMessage));
               }

               return true;
            }

            sender.sendMessage(ChatColor.RED + "Invalid page number! Total pages: " + totalPages);
            return true;
         }

         if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("clientbrand.reload")) {
               sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
               return true;
            }

            try {
               this.reloadConfig();
               if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
                  if (this.placeholderExpansion != null) {
                     this.placeholderExpansion.unregister();
                  }

                  this.placeholderExpansion = new ClientBrandExpansion(this);
                  this.placeholderExpansion.register();
               }

               String reloadMessage = this.getConfig().getString("reload-message", "&8[&bClientBrand&8] &aConfiguration reloaded successfully!");
               sender.sendMessage(ChatColor.translateAlternateColorCodes('&', reloadMessage));
            } catch (Exception var17) {
               sender.sendMessage(ChatColor.RED + "Error reloading configuration: " + var17.getMessage());
               this.getLogger().warning("Error reloading configuration: " + var17.getMessage());
            }

            return true;
         }
      }

      return false;
   }

   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      if (command.getName().equalsIgnoreCase("clientbrand")) {
         ArrayList completions;
         if (args.length == 1) {
            completions = new ArrayList();
            if (sender.hasPermission("clientbrand.get")) {
               completions.add("get");
            }

            if (sender.hasPermission("clientbrand.list")) {
               completions.add("list");
            }

            if (sender.hasPermission("clientbrand.reload")) {
               completions.add("reload");
            }

            return completions;
         }

         if (args.length == 2 && args[0].equalsIgnoreCase("get")) {
            completions = new ArrayList();
            Iterator var6 = Bukkit.getOnlinePlayers().iterator();

            while(var6.hasNext()) {
               Player p = (Player)var6.next();
               completions.add(p.getName());
            }

            return completions;
         }
      }

      return null;
   }
}
