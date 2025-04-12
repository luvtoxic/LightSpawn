package dev.luvtoxic.lightSpawn;

import dev.luvtoxic.lightSpawn.commands.SpawnCommands;
import dev.luvtoxic.lightSpawn.config.SpawnConfig;
import dev.luvtoxic.lightSpawn.data.LocationData;
import dev.luvtoxic.lightSpawn.listeners.JoinListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public final class LightSpawn extends JavaPlugin {

  private LocationData locationData;
  private SpawnConfig spawnConfig;
  private SpawnCommands spawnCommands;

  @Override
  public void onEnable() {
    spawnConfig = new SpawnConfig(this);
    locationData = new LocationData(
            this,
            new File(getDataFolder(), "spawns.json"),
            Executors.newSingleThreadExecutor(r -> {
              Thread t = new Thread(r, "LightSpawn-Data");
              t.setDaemon(true);
              return t;
            }),
            new ConcurrentHashMap<>(),
            spawnConfig
    );

    getServer().getPluginManager().registerEvents(new JoinListener(locationData, spawnConfig), this);
    spawnCommands = new SpawnCommands(this, locationData, spawnConfig);
    spawnCommands.register();
  }

  @Override
  public void onDisable() {
    locationData.shutdown();
    spawnConfig.shutdown();  }
}
