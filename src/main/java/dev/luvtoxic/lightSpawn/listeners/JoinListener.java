package dev.luvtoxic.lightSpawn.listeners;

import dev.luvtoxic.lightSpawn.config.SpawnConfig;
import dev.luvtoxic.lightSpawn.data.LocationData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {



  private final LocationData locationData;
  private final SpawnConfig spawnConfig;

  public JoinListener(LocationData locationData, SpawnConfig spawnConfig) {
    this.locationData = locationData;
    this.spawnConfig = spawnConfig;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();

    if (!player.hasPlayedBefore() && spawnConfig.isFirstJoinSpawn()) {
      locationData.getSpawn(spawnConfig.getFirstJoinSpawnKey())
              .thenAccept(loc -> {
                if (loc != null) {
                  player.teleportAsync(loc);
                }
              });
      return;
    }

    String key = spawnConfig.isAlwaysJoinSpawn() ? "main" : spawnConfig.getFallbackSpawnKey();
    locationData.getEffectiveSpawn(player.getUniqueId(), key)
            .thenAccept(loc -> {
              if (loc != null) {
                player.teleportAsync(loc);
              }
            });
  }
}