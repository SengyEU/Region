package cz.sengycraft.region.regions;

import cz.sengycraft.region.regions.flags.Flag;
import cz.sengycraft.region.regions.flags.FlagRegistry;
import cz.sengycraft.region.regions.flags.FlagState;
import cz.sengycraft.region.storage.DatabaseOperations;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Region {

    private String name;
    private Location pos1;
    private Location pos2;
    private Map<Flag, FlagState> flags = new HashMap<>();
    private List<String> whitelistedPlayers = new ArrayList<>();

    DatabaseOperations databaseOperations = DatabaseOperations.getInstance();

    public Region(String name, Location pos1, Location pos2) throws Exception {
        this.name = name;
        this.pos1 = pos1;
        this.pos2 = pos2;
        addFlags(FlagRegistry.getInstance().getFlags().toArray(Flag[]::new));
    }

    public Region(String name, Location pos1, Location pos2, Map<Flag, FlagState> flags, List<String> whitelistedPlayers) {
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
        DatabaseOperations.getInstance().updateRegionName(this.name, name);
        this.name = name;
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

    public void addFlags(Flag... flags) {
        for (Flag flag : flags) {
            this.flags.put(flag, RegionManager.getDefaultState());
            DatabaseOperations.getInstance().addFlag(name, flag, RegionManager.getDefaultState());
        }
    }

    public List<String> getWhitelistedPlayers() {
        return whitelistedPlayers;
    }

    public void addWhitelistedPlayer(String whitelistedPlayer) throws Exception {
        this.whitelistedPlayers.add(whitelistedPlayer);
        databaseOperations.addPlayerToWhitelist(name, whitelistedPlayer);
    }

    public void removeWhitelistedPlayer(String whitelistedPlayer) throws Exception {
        this.whitelistedPlayers.remove(whitelistedPlayer);
        databaseOperations.removePlayerFromWhitelist(name, whitelistedPlayer);
    }

    public boolean isPlayerWhitelisted(String player) {
        return whitelistedPlayers.contains(player);
    }

    public void changeState(String flag, FlagState state) throws Exception {
        flags.put(new Flag(flag), state);
        databaseOperations.updateFlagState(this.name, flag, state);
    }

    public boolean contains(Location location) {
        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        return x >= minX && x <= maxX &&
                y >= minY && y <= maxY &&
                z >= minZ && z <= maxZ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Region flag = (Region) o;
        return name.equals(flag.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
