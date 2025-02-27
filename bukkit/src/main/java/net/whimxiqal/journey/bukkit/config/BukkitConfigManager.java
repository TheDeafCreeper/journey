/*
 * MIT License
 *
 * Copyright (c) whimxiqal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.whimxiqal.journey.bukkit.config;

import net.whimxiqal.journey.Journey;
import net.whimxiqal.journey.config.ConfigManager;
import net.whimxiqal.journey.config.Setting;
import net.whimxiqal.journey.config.Settings;
import net.whimxiqal.journey.bukkit.JourneyBukkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bukkit.configuration.InvalidConfigurationException;

/**
 * The Spigot implementation of the config manager.
 */
public class BukkitConfigManager implements ConfigManager {

  private final String fileName;

  private BukkitConfigManager(String fileName) {
    this.fileName = fileName;
  }

  /**
   * Creates a new configuration manager and runs {@link #load()} and {@link #save()} consecutively.
   * This loads all config data into memory, then saves the config settings to the file,
   * the last of which adds the previously absent settings to the file with defaults.
   *
   * @param fileName the file name of the config file
   * @return a new config manager
   */
  public static BukkitConfigManager initialize(String fileName) {
    BukkitConfigManager configManager = new BukkitConfigManager(fileName);
    configManager.load();
    configManager.save();
    return configManager;
  }

  private Map<String, Setting<?>> getSettings() {
    return Arrays.stream(Settings.class.getDeclaredFields())
        .map(field -> {
          try {
            return field.get(this);
          } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
          }
        })
        .filter(object -> object instanceof Setting)
        .map(object -> ((Setting<?>) object))
        .collect(Collectors.toMap(Setting::getPath, setting -> setting));
  }

  private void dumpSettings() {
    getSettings().forEach((path, setting) ->
        JourneyBukkit.get().getConfig().set(path, setting.printValue()));
  }

  @Override
  public void save() {
    File configFile = new File(JourneyBukkit.get().getDataFolder(), this.fileName);
    try {
      if (configFile.createNewFile()) {
        JourneyBukkit.get().getLogger().info("Created config file for Journey");
      }
      try (FileOutputStream fos = new FileOutputStream(configFile)) {
        // Stage static setting values into config object
        dumpSettings();
        // Write config to file
        fos.write(JourneyBukkit.get().getConfig().saveToString().getBytes(StandardCharsets.UTF_8));
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private void loadSettings() {
    getSettings().forEach((s, setting) -> {
      if (JourneyBukkit.get().getConfig().contains(s)) {
        parseAndSetValue(setting, s);
      } else {
        JourneyBukkit.get().getConfig().set(s, setting.getValue());
      }
    });
  }

  /**
   * Helper function to ensure proper typing.
   *
   * @param setting     the setting
   * @param settingPath the string version of the setting value
   * @param <X>         the type of setting
   */
  private <X> void parseAndSetValue(Setting<X> setting, String settingPath) {
    String configString = JourneyBukkit.get()
        .getConfig()
        .getString(settingPath);
    if (configString == null) {
      Journey.logger().error("Could not get config value for config setting: " + settingPath);
      return;
    }
    X value = setting.parseValue(configString);
    if (value == null) {
      setting.setValue(setting.getDefaultValue());
      Journey.logger().error("Invalid value " + configString + " for setting " + settingPath
      + ". Using default: " + setting.printValue());
    } else {
      setting.setValue(value);
    }
  }

  @Override
  public void load() {
    File configFile = new File(JourneyBukkit.get().getDataFolder(), this.fileName);
    try {
      if (configFile.createNewFile()) {
        JourneyBukkit.get().getLogger().info("Created config file for Journey");
      }
      try (FileInputStream fis = new FileInputStream(configFile)) {
        byte[] data = new byte[(int) configFile.length()];
        if (fis.read(data) < 0) {
          JourneyBukkit.get().getLogger().severe("Configuration file could not be read");
        }
        // Stage into config object
        JourneyBukkit.get().getConfig().loadFromString(new String(data, StandardCharsets.UTF_8));
        // Put config object into static settings
        loadSettings();
      } catch (InvalidConfigurationException e) {
        JourneyBukkit.get().getLogger().severe("Your configuration file is malformed!");
        e.printStackTrace();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
