package dev.luvtoxic.lightSpawn.config;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigSerializer {

  public static ConfigData loadFromFile(Gson gson, File file) {
    try (FileReader reader = new FileReader(file)) {
      ConfigData data = gson.fromJson(reader, ConfigData.class);
      return data != null ? data : new ConfigData();
    } catch (IOException e) {
      return new ConfigData();
    }
  }

  public static void saveToFile(Gson gson, File file, ConfigData data) {
    try (FileWriter writer = new FileWriter(file)) {
      gson.toJson(data, writer);
    } catch (IOException e) {
      // User will handle
    }
  }

}
