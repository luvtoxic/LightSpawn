package dev.luvtoxic.lightSpawn.data;

import org.bukkit.Location;

public class CachedLocation {

  private final Location location;
  private final SpawnType type;
  private long lastAccess;

  public CachedLocation(Location location, SpawnType type) {
    this.location = location;
    this.type = type;
  }

  public Location getLocation() {
    lastAccess = System.currentTimeMillis();
    return location;
  }

  public SpawnType getType() {
    return type;
  }

  public long getLastAccess() {
    return lastAccess;
  }
}
