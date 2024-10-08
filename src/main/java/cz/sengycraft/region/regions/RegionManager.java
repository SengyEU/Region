package cz.sengycraft.region.regions;

import cz.sengycraft.region.configuration.ConfigurationManager;
import cz.sengycraft.region.regions.flags.Flag;
import cz.sengycraft.region.regions.flags.FlagState;
import cz.sengycraft.region.storage.DatabaseOperations;

import java.util.HashSet;
import java.util.Set;

public class RegionManager {

    private static RegionManager instance;

    public static RegionManager getInstance() {
        if (instance == null) instance = new RegionManager();

        return instance;
    }

    private Set<Region> regions = new HashSet<>();

    public void addFlags(Flag ... flags) {
        for(Region region : regions) {
            region.addFlags(flags);
        }
    }

    public Region getRegion(String name) {
        return regions.stream()
                .filter(region -> region.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public void addAllRegions() throws Exception {
        regions.addAll(DatabaseOperations.getInstance().getAllRegions());
    }

    public boolean addRegions(Region ... regions) throws Exception {
        for(Region region : regions) {
            if(createRegion(region)) return true;
        }

        return false;
    }

    public boolean createRegion(Region region) throws Exception {
        if(regions.add(region)) {
            DatabaseOperations.getInstance().saveRegion(region);
            return true;
        }

        return false;
    }

    public void deleteRegion(String name) throws Exception {
        regions.removeIf(region -> region.getName().equalsIgnoreCase(name));
        DatabaseOperations.getInstance().deleteRegion(name);
    }

    public static FlagState getDefaultState() {
        try {

            String configState = ConfigurationManager.getInstance()
                    .getConfiguration("config")
                    .getString("regions.default-state");

            if(configState == null) throw new IllegalArgumentException();

            return FlagState.valueOf(configState.toUpperCase());

        } catch (IllegalArgumentException e) {
            return FlagState.WHITELIST;
        }
    }

}
