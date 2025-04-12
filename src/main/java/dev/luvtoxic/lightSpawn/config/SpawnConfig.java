package dev.luvtoxic.lightSpawn.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.luvtoxic.lightSpawn.data.CachedLocation;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class SpawnConfig {

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private final JavaPlugin plugin;
  private final File configFile;
  private final Executor asyncExecutor;
  private ConfigData configData;
  private volatile boolean isLoading;

  public SpawnConfig(JavaPlugin plugin) {
    this.plugin = plugin;
    this.configFile = new File(plugin.getDataFolder(), "spawn-config.json");
    this.asyncExecutor = Executors.newSingleThreadExecutor(r -> {
      Thread thread = new Thread(r, "LightSpawn-Config-Thread");
      thread.setDaemon(true);
      return thread;
    });
    this.configData = new ConfigData();
    this.isLoading = false;
    initialize();
  }


  private void initialize() {
    CompletableFuture.runAsync(() -> {
      try {
        if (!configFile.exists()) {
          plugin.getDataFolder().mkdirs();
          configFile.createNewFile();
          ConfigSerializer.saveToFile(GSON, configFile, configData);
        }
        configData = ConfigSerializer.loadFromFile(GSON, configFile);
      } catch (Exception e) {
        plugin.getLogger().log(Level.SEVERE, "Failed to initialize spawn config: ", e);
      } finally {
        isLoading = false;
      }
    }, asyncExecutor);
  }

  public boolean isAlwaysJoinSpawn() {
    return configData.isAlwaysJoinSpawn();
  }

  public CompletableFuture<Void> setAlwaysJoinSpawn(boolean enabled) {
    return CompletableFuture.runAsync(() -> {
      while (isLoading) {
        Thread.onSpinWait();
      }
      configData.setAlwaysJoinSpawn(enabled);
      ConfigSerializer.saveToFile(GSON, configFile, configData);
    }, asyncExecutor);
  }

  public boolean isFirstJoinSpawn() {
    return configData.isFirstJoinSpawn();
  }

  public CompletableFuture<Void> setFirstJoinSpawn(boolean enabled) {
    return CompletableFuture.runAsync(() -> {
      while (isLoading) {
        Thread.onSpinWait();
      }
      configData.setFirstJoinSpawn(enabled);
      ConfigSerializer.saveToFile(GSON, configFile, configData);
    }, asyncExecutor);
  }

  public boolean isBedSpawnEnabled() {
    return configData.isBedSpawnEnabled();
  }

  public CompletableFuture<Void> setBedSpawnEnabled(boolean enabled) {
    return CompletableFuture.runAsync(() -> {
      while (isLoading) {
        Thread.onSpinWait();
      }
      configData.setBedSpawnEnabled(enabled);
      ConfigSerializer.saveToFile(GSON, configFile, configData);
    }, asyncExecutor);
  }

  public String getFirstJoinSpawnKey() {
    return configData.getFirstJoinSpawnKey();
  }

  public CompletableFuture<Void> setFirstJoinSpawnKey(String key) {
    return CompletableFuture.runAsync(() -> {
      while (isLoading) {
        Thread.onSpinWait();
      }
      configData.setFirstJoinSpawnKey(key);
      ConfigSerializer.saveToFile(GSON, configFile, configData);
    }, asyncExecutor);
  }

  public String getFallbackSpawnKey() {
    return configData.getFallbackSpawnKey();
  }

  public CompletableFuture<Void> setFallbackSpawnKey(String key) {
    return CompletableFuture.runAsync(() -> {
      while (isLoading) {
        Thread.onSpinWait();
      }
      configData.setFallbackSpawnKey(key);
      ConfigSerializer.saveToFile(GSON, configFile, configData);
    }, asyncExecutor);
  }

  public void shutdown() {
    ConfigSerializer.saveToFile(GSON, configFile, configData);
//    asyncExecutor.notify();
  }

  public void reload() {
    CompletableFuture.runAsync(() -> {
      isLoading = true;
      try {
        configData = ConfigSerializer.loadFromFile(GSON, configFile);
      } catch (Exception e) {
        plugin.getLogger().log(Level.SEVERE, "Failed to reload spawn config", e);
        throw new RuntimeException("Reload failed", e);
      } finally {
        isLoading = false;
      }
    }, asyncExecutor).join();
  }
}
