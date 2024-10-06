package cz.sengycraft.region;

import cz.sengycraft.region.api.RegionAPI;
import cz.sengycraft.region.configuration.ConfigurationManager;
import cz.sengycraft.region.storage.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class RegionPlugin extends JavaPlugin {

    private RegionAPI regionAPI;

    public RegionAPI getRegionAPI() {
        return regionAPI;
    }

    @Override
    public void onEnable() {
        ConfigurationManager configurationManager = ConfigurationManager.getInstance();
        configurationManager.setPlugin(this);
        configurationManager.initializeConfigurations(v -> {
            getComponentLogger().info("Configuration files successfully initialized!");

            DatabaseManager databaseManager = DatabaseManager.getInstance();
            databaseManager.setPlugin(this);
            databaseManager.initializeDatabase(e -> {
                if (e == null) {
                    getComponentLogger().info("Database connection successfully established!");
                } else {
                    getComponentLogger().error("Couldn't connect to the database!", e);
                }
            });
        }, "config");

        regionAPI = new RegionAPI();

    }

    @Override
    public void onDisable() {
        DatabaseManager.getInstance().closeDatabase();
    }

}
