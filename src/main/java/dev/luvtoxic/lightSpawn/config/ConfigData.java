package dev.luvtoxic.lightSpawn.config;

public class ConfigData {

  private boolean alwaysJoinSpawn;
  private boolean firstJoinSpawn;
  private boolean bedSpawnEnabled;
  private String firstJoinSpawnKey;
  private String fallbackSpawnKey;

  public ConfigData() {
    this.alwaysJoinSpawn = false;
    this.firstJoinSpawn = true;
    this.bedSpawnEnabled = true;
    this.firstJoinSpawnKey = "first_join";
    this.fallbackSpawnKey = "main";
  }

  public boolean isAlwaysJoinSpawn() {
    return alwaysJoinSpawn;
  }

  public void setAlwaysJoinSpawn(boolean alwaysJoinSpawn) {
    this.alwaysJoinSpawn = alwaysJoinSpawn;
  }

  public boolean isFirstJoinSpawn() {
    return firstJoinSpawn;
  }

  public void setFirstJoinSpawn(boolean firstJoinSpawn) {
    this.firstJoinSpawn = firstJoinSpawn;
  }

  public boolean isBedSpawnEnabled() {
    return bedSpawnEnabled;
  }

  public void setBedSpawnEnabled(boolean bedSpawnEnabled) {
    this.bedSpawnEnabled = bedSpawnEnabled;
  }

  public String getFirstJoinSpawnKey() {
    return firstJoinSpawnKey;
  }

  public void setFirstJoinSpawnKey(String firstJoinSpawnKey) {
    this.firstJoinSpawnKey = firstJoinSpawnKey != null ? firstJoinSpawnKey : "first_join";
  }

  public String getFallbackSpawnKey() {
    return fallbackSpawnKey;
  }

  public void setFallbackSpawnKey(String fallbackSpawnKey) {
    this.fallbackSpawnKey = fallbackSpawnKey != null ? fallbackSpawnKey : "main";
  }

}
