package cz.sengycraft.region.configuration;

import cz.sengycraft.region.RegionPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ConfigurationManager {

    private RegionPlugin plugin;

    public void setPlugin(RegionPlugin plugin) {
        this.plugin = plugin;
    }

    private static ConfigurationManager instance;

    public static ConfigurationManager getInstance() {
        if (instance == null) instance = new ConfigurationManager();

        return instance;
    }

    private final Map<String, FileConfiguration> configurations = new ConcurrentHashMap<>();

    public void initializeConfigurations(Consumer<Void> onComplete, String... fileNames) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            for (String fileName : fileNames) {
                FileConfiguration fileConfiguration;
                File configFile = new File(plugin.getDataFolder(), fileName + ".yml");
                if (!configFile.exists()) plugin.saveResource(fileName + ".yml", false);

                fileConfiguration = YamlConfiguration.loadConfiguration(configFile);

                configurations.put(fileName, fileConfiguration);
            }

            onComplete.accept(null);
        });
    }

    public FileConfiguration getConfiguration(String name) {
        return configurations.get(name);
    }

    public void reloadConfigurations(Consumer<Exception> callback) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            for (Map.Entry<String, FileConfiguration> entry : configurations.entrySet()) {

                String fileName = entry.getKey();
                FileConfiguration configuration = entry.getValue();
                File configFile = new File(plugin.getDataFolder(), fileName + ".yml");

                try {
                    configuration.load(configFile);
                } catch (Exception e) {
                    callback.accept(e);
                }

            }
        });
    }


}
