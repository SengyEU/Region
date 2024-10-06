package cz.sengycraft.region.regions;

import cz.sengycraft.region.regions.flags.Flag;
import cz.sengycraft.region.regions.flags.FlagRegistry;
import cz.sengycraft.region.regions.flags.FlagState;
import cz.sengycraft.region.storage.DatabaseOperations;
import org.bukkit.Location;

import java.util.*;

public class Region {

    private String name;
    private Location pos1;
    private Location pos2;
    private Map<Flag, FlagState> flags = new HashMap<>();
    private List<UUID> whitelistedPlayers = new ArrayList<>();

    DatabaseOperations databaseOperations = DatabaseOperations.getInstance();

    public Region(String name, Location pos1, Location pos2) throws Exception {
        this.name = name;
        this.pos1 = pos1;
        this.pos2 = pos2;
        addFlags(FlagRegistry.getInstance().getFlags().toArray(Flag[]::new));
    }

    public Region(String name, Location pos1, Location pos2, Map<Flag, FlagState> flags, List<UUID> whitelistedPlayers) {
        this.name = name;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.flags = flags;
        this.whitelistedPlayers = whitelistedPlayers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws Exception {
        this.name = name;
        databaseOperations.updateRegionName(this.name, name);
    }

    public Location getPos1() {
        return pos1;
    }

    public void setPos1(Location pos1) throws Exception {
        this.pos1 = pos1;
        databaseOperations.updateRegionPos(this.name, true, pos1);
    }

    public Location getPos2() {
        return pos2;
    }

    public void setPos2(Location pos2) throws Exception {
        this.pos2 = pos2;
        databaseOperations.updateRegionPos(this.name, false, pos2);
    }

    public Map<Flag, FlagState> getFlags() {
        return flags;
    }

    public void addFlags(Flag ... flags) {
        for(Flag flag : flags) {
            this.flags.put(flag, RegionManager.getDefaultState());
        }
    }

    public List<UUID> getWhitelistedPlayers() {
        return whitelistedPlayers;
    }

    public void addWhitelistedPlayer(UUID whitelistedPlayer) {
        this.whitelistedPlayers.add(whitelistedPlayer);
    }

    public boolean isPlayerWhitelisted(UUID player) {
        return whitelistedPlayers.contains(player);
    }
}
