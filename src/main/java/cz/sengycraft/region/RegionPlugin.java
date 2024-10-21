package cz.sengycraft.region;

import cz.sengycraft.region.api.RegionAPI;
import cz.sengycraft.region.commands.RegionCommand;
import cz.sengycraft.region.configuration.ConfigurationManager;
import cz.sengycraft.region.listeners.FlagsListeners;
import cz.sengycraft.region.listeners.RenameListener;
import cz.sengycraft.region.regions.RegionManager;
import cz.sengycraft.region.regions.flags.Flag;
import cz.sengycraft.region.regions.flags.FlagRegistry;
import cz.sengycraft.region.regions.wand.WandManager;
import cz.sengycraft.region.storage.DatabaseManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class RegionPlugin extends JavaPlugin {

    private RegionAPI regionAPI;

    public RegionAPI getRegionAPI() {
        return regionAPI;
    }

    @Override
    public void onEnable() {

        WandManager.getInstance().setPlugin(this);

        regionAPI = new RegionAPI();

        getServer().getPluginManager().registerEvents(WandManager.getInstance(), this);
        getServer().getPluginManager().registerEvents(new FlagsListeners(), this);
        getServer().getPluginManager().registerEvents(RenameListener.getInstance(), this);
        getCommand("region").setExecutor(new RegionCommand(this));

        FlagRegistry.getInstance().addFlags(
                new Flag("block-break"),
                new Flag("block-place"),
                new Flag("interact"),
                new Flag("entity-damage")
        );

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

            RegionManager.getInstance().setPlugin(this);
            RegionManager.getInstance().addAllRegions(loadedRegions -> getComponentLogger().info("Regions loaded: " + loadedRegions.size()));
        }, "config", "messages");

    }

    @Override
    public void onDisable() {
        DatabaseManager.getInstance().closeDatabase();
    }

}
