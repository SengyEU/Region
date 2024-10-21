package cz.sengycraft.region.regions.flags;

import cz.sengycraft.region.regions.RegionManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FlagRegistry {

    private static FlagRegistry instance;

    public static FlagRegistry getInstance() {
        if (instance == null) instance = new FlagRegistry();
        return instance;
    }

    Set<Flag> flags = new HashSet<>();

    public void addFlags(Flag... flags) {
        Collections.addAll(this.flags, flags);
        RegionManager.getInstance().addFlags(flags);
    }

    public Set<Flag> getFlags() {
        return flags;
    }
}
