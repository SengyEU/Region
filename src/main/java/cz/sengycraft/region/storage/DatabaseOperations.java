package cz.sengycraft.region.storage;

import com.zaxxer.hikari.HikariDataSource;
import cz.sengycraft.region.RegionPlugin;
import cz.sengycraft.region.regions.Region;
import cz.sengycraft.region.regions.flags.Flag;
import cz.sengycraft.region.regions.flags.FlagState;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class DatabaseOperations {

    private static DatabaseOperations instance;

    public static DatabaseOperations getInstance() {
        if (instance == null) instance = new DatabaseOperations();
        return instance;
    }

    DatabaseManager databaseManager = DatabaseManager.getInstance();
    HikariDataSource hikariDataSource = databaseManager.getHikariDataSource();
    RegionPlugin plugin = databaseManager.getPlugin();

    public Region getRegion(String regionName) throws Exception {

        Location pos1 = null;
        Location pos2 = null;
        Map<Flag, FlagState> flags = new HashMap<>();
        List<UUID> whitelistedPlayers = new ArrayList<>();

       Connection connection = hikariDataSource.getConnection();
       PreparedStatement regionStatement = connection.prepareStatement("SELECT * FROM regions WHERE name = ?");

       regionStatement.setString(1, regionName);
       ResultSet regionResult = regionStatement.executeQuery();

       if (regionResult.next()) {
           String world = regionResult.getString("pos1World");
           pos1 = new Location(
                   Bukkit.getWorld(world),
                   regionResult.getDouble("pos1X"),
                   regionResult.getDouble("pos1Y"),
                   regionResult.getDouble("pos1Z")
           );

           world = regionResult.getString("pos2World");
           pos2 = new Location(
                   Bukkit.getWorld(world),
                   regionResult.getDouble("pos2X"),
                   regionResult.getDouble("pos2Y"),
                   regionResult.getDouble("pos2Z")
           );
       }

       PreparedStatement flagStatement = connection.prepareStatement("SELECT * FROM region_flags WHERE region_name = ?");

       flagStatement.setString(1, regionName);
       ResultSet flagResult = flagStatement.executeQuery();

       while (flagResult.next()) {
           Flag flag = new Flag(flagResult.getString("flag"));
           FlagState flagState = FlagState.valueOf(flagResult.getString("state"));
           flags.put(flag, flagState);
       }

       PreparedStatement whitelistStatement = connection.prepareStatement("SELECT * FROM region_whitelist WHERE region_name = ?");

       whitelistStatement.setString(1, regionName);
       ResultSet whitelistResult = whitelistStatement.executeQuery();

       while (whitelistResult.next()) {
           UUID playerUUID = UUID.fromString(whitelistResult.getString("player_uuid"));
           whitelistedPlayers.add(playerUUID);
       }

       return new Region(regionName, pos1, pos2, flags, whitelistedPlayers);
    }

    public void updateRegionName(String regionName, String newName) throws Exception {
        String sql = "UPDATE regions SET name = ? WHERE name = ?";
        Connection connection = hikariDataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, newName);
        statement.setString(2, regionName);
        statement.executeUpdate();
    }

    public void saveRegion(Region region) throws Exception {
        String sql = "INSERT INTO regions (name, pos1X, pos1Y, pos1Z, pos1World, pos2X, pos2Y, pos2Z, pos2World) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection connection = hikariDataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, region.getName());
        statement.setDouble(2, region.getPos1().getX());
        statement.setDouble(3, region.getPos1().getY());
        statement.setDouble(4, region.getPos1().getZ());
        statement.setString(5, region.getPos1().getWorld().getName());
        statement.setDouble(6, region.getPos2().getX());
        statement.setDouble(7, region.getPos2().getY());
        statement.setDouble(8, region.getPos2().getZ());
        statement.setString(9, region.getPos2().getWorld().getName());

        statement.executeUpdate();

        String insertFlagSql = "INSERT INTO region_flags (region_name, flag, state) VALUES (?, ?, ?)";
        PreparedStatement insertFlagStmt = connection.prepareStatement(insertFlagSql);

        for (Map.Entry<Flag, FlagState> entry : region.getFlags().entrySet()) {
            insertFlagStmt.setString(1, region.getName());
            insertFlagStmt.setString(2, entry.getKey().getName());
            insertFlagStmt.setString(3, entry.getValue().toString());
        }
        insertFlagStmt.executeUpdate();

        String insertWhitelistSql = "INSERT INTO region_whitelist (region_name, player_uuid) VALUES (?, ?)";
        PreparedStatement insertWhitelistStmt = connection.prepareStatement(insertWhitelistSql);

        for (UUID playerUUID : region.getWhitelistedPlayers()) {
            insertWhitelistStmt.setString(1, region.getName());
            insertWhitelistStmt.setString(2, playerUUID.toString());
        }
        insertWhitelistStmt.executeUpdate();
    }

    public void updateRegionPos(String regionName, boolean isPos1, Location loc) throws Exception {
        String pos = isPos1 ? "1" : "2";

        String sql = "UPDATE regions SET pos" + pos + "X = ?, pos" + pos + "Y = ?, pos" + pos + "Z = ?, pos" + pos + "World = ? WHERE name = ?";

        Connection connection = hikariDataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setDouble(1, loc.getX());
        statement.setDouble(2, loc.getY());
        statement.setDouble(3, loc.getZ());
        statement.setString(4, loc.getWorld().getName());
        statement.setString(5, regionName);

        statement.executeUpdate();
    }


    public void deleteRegion(String regionName) throws Exception {
        String sql = "DELETE FROM regions WHERE name = ?";
        Connection connection = hikariDataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, regionName);
        statement.executeUpdate();
    }

    public void addPlayerToWhitelist(String regionName, UUID playerUUID) throws Exception {
        String sql = "INSERT INTO region_whitelist (region_name, player_uuid) VALUES (?, ?)";
        Connection connection = hikariDataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, regionName);
        statement.setString(2, playerUUID.toString());
        statement.executeUpdate();

    }

    public void removePlayerFromWhitelist(String regionName, UUID playerUUID) throws Exception {
        String sql = "DELETE FROM region_whitelist WHERE region_name = ? AND player_uuid = ?";
        Connection connection = hikariDataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, regionName);
        statement.setString(2, playerUUID.toString());
        statement.executeUpdate();
    }
}
