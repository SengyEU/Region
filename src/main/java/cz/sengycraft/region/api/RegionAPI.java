package cz.sengycraft.region.api;

import cz.sengycraft.region.regions.flags.FlagRegistry;

public class RegionAPI {

    public FlagRegistry getFlagRegistry() {
        return FlagRegistry.getInstance();
    }

}
