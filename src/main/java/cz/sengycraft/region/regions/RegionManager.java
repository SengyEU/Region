package cz.sengycraft.region.regions;

import cz.sengycraft.region.RegionPlugin;
import cz.sengycraft.region.configuration.ConfigurationManager;
import cz.sengycraft.region.regions.flags.Flag;
import cz.sengycraft.region.regions.flags.FlagState;
import cz.sengycraft.region.storage.DatabaseOperations;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RegionManager {

    private static RegionManager instance;

    public static RegionManager getInstance() {
        if (instance == null) instance = new RegionManager();

        return instance;
    }

    private RegionPlugin plugin;

    public void setPlugin(RegionPlugin plugin) {
        this.plugin = plugin;
    }

    private Set<Region> regions = new HashSet<>();

    public void addFlags(Flag... flags) {
        for (Region region : regions) {
            region.addFlags(flags);
        }
    }

    public Region getRegion(String name) {
        return regions.stream()
                .filter(region -> region.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public void addAllRegions(Consumer<Set<Region>> callback) {
        DatabaseOperations.getInstance().getAllRegions(loadedRegions -> {
            regions.addAll(loadedRegions);
            callback.accept(regions);
        });
    }


    public Set<Region> getRegions() {
        return regions;
    }

    public boolean addRegion(Region region) {
        return createRegion(region);
    }


    public boolean createRegion(Region region) {
        if (regions.add(region)) {
            DatabaseOperations.getInstance().saveRegion(region);
            return true;
        }

        return false;
    }

    public List<String> getNames() {
        return regions.stream().map(Region::getName).collect(Collectors.toList());
    }

    public static FlagState getDefaultState() {
        try {

            String configState = ConfigurationManager.getInstance()
                    .getConfiguration("config")
                    .getString("regions.default-state");

            if (configState == null) throw new IllegalArgumentException();

            return FlagState.valueOf(configState.toUpperCase());

        } catch (IllegalArgumentException e) {
            return FlagState.WHITELIST;
        }
    }

}
