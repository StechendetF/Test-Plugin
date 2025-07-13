package deFelix.testplugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private final JavaPlugin plugin;
    private File configFile;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }


    private void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");


        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }


        config = YamlConfiguration.loadConfiguration(configFile);
    }


    public FileConfiguration getConfig() {
        return config;
    }


    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
    }
}
