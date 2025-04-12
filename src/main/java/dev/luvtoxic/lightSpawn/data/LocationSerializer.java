package dev.luvtoxic.lightSpawn.data;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocationSerializer {

  private static class LocationDTO {
    private String world;
    private double x, y, z;
    private float yaw, pitch;
    private String type;

    LocationDTO(Location location, SpawnType type) {
      this.world = location.getWorld().getName();
      this.x = location.getX();
      this.y = location.getY();
      this.z = location.getZ();
      this.yaw = location.getYaw();
      this.pitch = location.getPitch();
      this.type = type.name();
    }

    Location toLocation() {
      World world = Bukkit.getWorld(this.world);
      if (world == null) return null;
      return new Location(world, x, y, z, yaw, pitch);
    }

    SpawnType getType() {
      return SpawnType.valueOf(type);
    }
  }

  public static Map<String, CachedLocation> loadFromFile(Gson gson, File file) {
    Map<String, CachedLocation> result = new ConcurrentHashMap<>();
    try (FileReader reader = new FileReader(file)) {
      Map<String, LocationDTO> data = gson.fromJson(reader, new TypeToken<Map<String, LocationDTO>>() {}.getType());
      if (data != null) {
        data.forEach((key, dto) ->{
          Location loc = dto.toLocation();
          if (loc != null) {
            result.put(key, new CachedLocation(loc, dto.getType()));
          }
        });
      }
    } catch (IOException e) {
      // Done when called
    }
    return result;
  }

  public static void saveToFile(Gson gson, File file, Map<String, CachedLocation> cache) {

    try (FileWriter writer = new FileWriter(file)) {
      Map<String, LocationDTO> data = new ConcurrentHashMap<>();
      cache.forEach((key, cached) -> data.put(key, new LocationDTO(cached.getLocation(), cached.getType())));
      gson.toJson(data, writer);
    } catch (IOException e) {
      // Done when called
    }

  }


}
