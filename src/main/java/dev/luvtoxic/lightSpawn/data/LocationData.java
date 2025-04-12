package dev.luvtoxic.lightSpawn.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.luvtoxic.lightSpawn.config.SpawnConfig;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class LocationData {

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private final JavaPlugin plugin;
  private final SpawnConfig config;
  private final File dataFile;
  private final Executor asyncExecutor;
  private final Map<String, CachedLocation> locationCache;
  private volatile boolean isLoading;

  public LocationData(JavaPlugin plugin, File dataFile, Executor asyncExecutor, Map<String, CachedLocation> locationCache, SpawnConfig config) {
    this.plugin = plugin;
    this.dataFile = new File(plugin.getDataFolder(), "spawns.json");
    this.config = config;
    this.asyncExecutor = Executors.newSingleThreadExecutor(r -> {
      Thread thread = new Thread(r, "LightSpawn-Data-Thread");
      thread.setDaemon(true);
      return thread;
    });
    this.locationCache = new ConcurrentHashMap<>();
    this.isLoading = false;
    initialize();
  }

  private void initialize() {

    CompletableFuture.runAsync(() -> {
      try {
        if (!dataFile.exists()) {
          plugin.getDataFolder().mkdirs();
          dataFile.createNewFile();
          LocationSerializer.saveToFile(GSON, dataFile, new ConcurrentHashMap<>());
        }
        locationCache.putAll(LocationSerializer.loadFromFile(GSON, dataFile));
      } catch (IOException e) {
        plugin.getLogger().log(Level.SEVERE, "Failed to initialize spawn data, Error below: ");
        throw new RuntimeException(e);
      } finally {
        isLoading = false;
      }
    }, asyncExecutor);
  }

  /**
   * Checks if a spawn location is set
   * @param key
   * @return
   */
  public CompletableFuture<Boolean> isSpawnSet(String key) {
    return CompletableFuture.supplyAsync(() -> locationCache.containsKey(key), asyncExecutor);
  }


  /**
   * Save a spawn location
   * @param key
   * @param location
   * @param type
   * @return
   */
  public CompletableFuture<Void> saveSpawn(String key, Location location, SpawnType type) {
    if (location == null || location.getWorld() == null) {
      return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid location"));
    }

    return CompletableFuture.runAsync(() -> {
      while (isLoading) {
        Thread.onSpinWait();
      }
      locationCache.put(key, new CachedLocation(location.clone(), type));
      LocationSerializer.saveToFile(GSON, dataFile, locationCache);
    }, asyncExecutor);
  }

  /**
   * Get a spawn location
   * @param key
   * @return
   */
  public CompletableFuture<Location> getSpawn(String key) {
    return CompletableFuture.supplyAsync(() -> {
      CachedLocation cached = locationCache.get(key);
      return cached != null ? cached.getLocation().clone() : null;
    }, asyncExecutor);
  }

  public CompletableFuture<Location> getEffectiveSpawn(UUID playerUUID, String key) {
    return CompletableFuture.supplyAsync(() -> {
      if (config.isBedSpawnEnabled()) {
        Location bedSpawn = plugin.getServer().getPlayer(playerUUID) != null
                ? plugin.getServer().getPlayer(playerUUID).getBedSpawnLocation()
                : null;
        if (bedSpawn != null && bedSpawn.getWorld() != null) {
          return bedSpawn.clone();
        }
      }

      CachedLocation cached = locationCache.get(key);
      if (cached != null) {
        return cached.getLocation().clone();
      }

      cached = locationCache.get(config.getFallbackSpawnKey());
      return cached != null ? cached.getLocation().clone() : null;
    }, asyncExecutor);
  }

  public CompletableFuture<SpawnType> getSpawnType(String key) {
    return CompletableFuture.supplyAsync(() -> {
      CachedLocation cached = locationCache.get(key);
      return cached != null ? cached.getType() : null;
    }, asyncExecutor);
  }

  public CompletableFuture<Void> deleteSpawn(String key) {
    return CompletableFuture.runAsync(() -> {
      locationCache.remove(key);
      LocationSerializer.saveToFile(GSON, dataFile, locationCache);
    }, asyncExecutor);
  }

  public void cleanCache() {
    CompletableFuture.runAsync(() -> {
      long currentTime = System.currentTimeMillis();
      locationCache.entrySet().removeIf(entry ->
              currentTime - entry.getValue().getLastAccess() > 3_600_000);
    }, asyncExecutor);
  }

  public void shutdown() {
    LocationSerializer.saveToFile(GSON, dataFile, locationCache);
  }

  public Set<String> getSpawnKeys() {
    return locationCache.keySet();
  }

  public void reload() {
    CompletableFuture.runAsync(() -> {
      isLoading = true;
      try {
        locationCache.clear();
        locationCache.putAll(LocationSerializer.loadFromFile(GSON, dataFile));
      } catch (Exception e) {
        plugin.getLogger().log(Level.SEVERE, "Failed to reload spawn data", e);
        throw new RuntimeException("Reload failed", e);
      } finally {
        isLoading = false;
      }
    }, asyncExecutor).join();
  }

}
