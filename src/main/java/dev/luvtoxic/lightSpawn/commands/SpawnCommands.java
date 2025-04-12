package dev.luvtoxic.lightSpawn.commands;

import dev.luvtoxic.lightSpawn.config.SpawnConfig;
import dev.luvtoxic.lightSpawn.data.LocationData;
import dev.luvtoxic.lightSpawn.data.SpawnType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SpawnCommands implements CommandExecutor, TabCompleter {

  private final JavaPlugin plugin;
  private final LocationData locationData;
  private final SpawnConfig spawnConfig;
  private static final String SETSPAWN_PERMISSION = "lightspawn.setspawn";
  private static final String SPAWN_PERMISSION = "lightspawn.spawn";
  private static final String RELOAD_PERMISSION = "lightspawn.reload";

  public SpawnCommands(JavaPlugin plugin, LocationData locationData, SpawnConfig spawnConfig) {
    this.plugin = plugin;
    this.locationData = locationData;
    this.spawnConfig = spawnConfig;
  }

  public void register() {
    plugin.getCommand("setspawn").setExecutor(this);
    plugin.getCommand("setspawn").setTabCompleter(this);
    plugin.getCommand("spawn").setExecutor(this);
    plugin.getCommand("spawn").setTabCompleter(this);
    plugin.getCommand("spawnreload").setExecutor(this);
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (command.getName().equalsIgnoreCase("setspawn")) {
      return handleSetSpawn(sender, args);
    } else if (command.getName().equalsIgnoreCase("spawn")) {
      return handleSpawn(sender, args);
    } else if (command.getName().equalsIgnoreCase("spawnreload")) {
      return handleReload(sender, args);
    }
    return false;
  }

  private boolean handleSetSpawn(CommandSender sender, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
      return true;
    }

    Player player = (Player) sender;
    if (!player.hasPermission(SETSPAWN_PERMISSION)) {
      player.sendMessage(ChatColor.RED + "You don't have permission to set spawns.");
      return true;
    }

    String key = args.length > 0 ? args[0] : "main";
    SpawnType type;
    try {
      type = args.length > 1 ? SpawnType.valueOf(args[1].toUpperCase()) : SpawnType.MAIN_SPAWN;
    } catch (IllegalArgumentException e) {
      player.sendMessage(ChatColor.RED + "Invalid spawn type. Valid types: " +
              Arrays.stream(SpawnType.values()).map(Enum::name).collect(Collectors.joining(", ")));
      return true;
    }

    Location location = player.getLocation();
    locationData.saveSpawn(key, location, type)
            .thenRun(() -> player.sendMessage(ChatColor.GREEN + "Spawn '" + key + "' set at your location."))
            .exceptionally(throwable -> {
              player.sendMessage(ChatColor.RED + "Failed to set spawn: " + throwable.getMessage());
              return null;
            });

    return true;
  }

  private boolean handleSpawn(CommandSender sender, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
      return true;
    }

    Player player = (Player) sender;
    if (!player.hasPermission(SPAWN_PERMISSION)) {
      player.sendMessage(ChatColor.RED + "You don't have permission to teleport to spawns.");
      return true;
    }

    String key = args.length > 0 ? args[0] : spawnConfig.getFallbackSpawnKey();
    locationData.getEffectiveSpawn(player.getUniqueId(), key)
            .thenAccept(location -> {
              if (location != null) {
                player.teleportAsync(location)
                        .thenRun(() -> player.sendMessage(ChatColor.GREEN + "Teleported to spawn '" + key + "'."));
              } else {
                player.sendMessage(ChatColor.RED + "Spawn '" + key + "' not found or unavailable.");
              }
            })
            .exceptionally(throwable -> {
              player.sendMessage(ChatColor.RED + "Failed to teleport: " + throwable.getMessage());
              return null;
            });

    return true;
  }

  private boolean handleReload(CommandSender sender, String[] args) {
    if (!sender.hasPermission(RELOAD_PERMISSION)) {
      sender.sendMessage(ChatColor.RED + "You don't have permission to reload spawn data.");
      return true;
    }

    CompletableFuture.runAsync(() -> {
      try {
        spawnConfig.reload();
        locationData.reload();
        sender.sendMessage(ChatColor.GREEN + "Spawn data and configuration reloaded successfully.");
      } catch (Exception e) {
        sender.sendMessage(ChatColor.RED + "Failed to reload spawn data: " + e.getMessage());
      }
    });

    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
    if (command.getName().equalsIgnoreCase("setspawn")) {
      if (args.length == 2) {
        return Arrays.stream(SpawnType.values())
                .map(Enum::name)
                .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList());
      }
    } else if (command.getName().equalsIgnoreCase("spawn")) {
      if (args.length == 1) {
        CompletableFuture<List<String>> future = CompletableFuture.supplyAsync(() -> {
          Set<String> keys = locationData.getSpawnKeys();
          return keys.stream()
                  .filter(key -> key.toLowerCase().startsWith(args[0].toLowerCase()))
                  .collect(Collectors.toList());
        });
        try {
          return future.join();
        } catch (Exception e) {
          return List.of("main", spawnConfig.getFirstJoinSpawnKey(), spawnConfig.getFallbackSpawnKey());
        }
      }
    }
    return List.of();
  }
}