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
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;

public class DatabaseOperations {

    private static DatabaseOperations instance;

    public static DatabaseOperations getInstance() {
        if (instance == null) instance = new DatabaseOperations();
        return instance;
    }

    DatabaseManager databaseManager = DatabaseManager.getInstance();
    HikariDataSource hikariDataSource = databaseManager.getHikariDataSource();
    RegionPlugin plugin = databaseManager.getPlugin();

    public void getAllRegions(Consumer<Set<Region>> callback) {

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Set<Region> allRegions = new HashSet<>();
            try (Connection connection = hikariDataSource.getConnection()) {
                PreparedStatement regionStatement = connection.prepareStatement("SELECT * FROM regions");
                ResultSet regionResult = regionStatement.executeQuery();

                while (regionResult.next()) {
                    String regionName = regionResult.getString("name");
                    Location pos1 = createLocation(regionResult, "pos1");
                    Location pos2 = createLocation(regionResult, "pos2");

                    Map<Flag, FlagState> flags = loadFlags(connection, regionName);
                    List<String> whitelistedPlayers = loadWhitelistedPlayers(connection, regionName);

                    allRegions.add(new Region(regionName, pos1, pos2, flags, whitelistedPlayers));
                }
                callback.accept(allRegions);
            } catch (SQLException e) {
                plugin.getComponentLogger().error("Couldn't load regions from database!", e);
            }
        });
    }

    private Location createLocation(ResultSet resultSet, String posPrefix) throws SQLException {
        return new Location(
                Bukkit.getWorld(resultSet.getString(posPrefix + "World")),
                resultSet.getDouble(posPrefix + "X"),
                resultSet.getDouble(posPrefix + "Y"),
                resultSet.getDouble(posPrefix + "Z")
        );
    }

    private Map<Flag, FlagState> loadFlags(Connection connection, String regionName) throws SQLException {
        Map<Flag, FlagState> flags = new HashMap<>();
        PreparedStatement flagStatement = connection.prepareStatement("SELECT * FROM region_flags WHERE region_name = ?");
        flagStatement.setString(1, regionName);
        ResultSet flagResult = flagStatement.executeQuery();

        while (flagResult.next()) {
            Flag flag = new Flag(flagResult.getString("flag"));
            FlagState flagState = FlagState.valueOf(flagResult.getString("state"));
            flags.put(flag, flagState);
        }
        return flags;
    }

    private List<String> loadWhitelistedPlayers(Connection connection, String regionName) throws SQLException {
        List<String> whitelistedPlayers = new ArrayList<>();
        PreparedStatement whitelistStatement = connection.prepareStatement("SELECT * FROM region_whitelist WHERE region_name = ?");
        whitelistStatement.setString(1, regionName);
        ResultSet whitelistResult = whitelistStatement.executeQuery();

        while (whitelistResult.next()) {
            String playerName = whitelistResult.getString("player_name");
            whitelistedPlayers.add(playerName);
        }
        return whitelistedPlayers;
    }

    public void saveRegion(Region region) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection connection = hikariDataSource.getConnection()) {
                String sql = "INSERT INTO regions (name, pos1X, pos1Y, pos1Z, pos1World, pos2X, pos2Y, pos2Z, pos2World) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
                    insertFlagStmt.executeUpdate();
                }

                if (!region.getWhitelistedPlayers().isEmpty()) {
                    String insertWhitelistSql = "INSERT INTO region_whitelist (region_name, player_name) VALUES (?, ?)";
                    PreparedStatement insertWhitelistStmt = connection.prepareStatement(insertWhitelistSql);

                    for (String playerName : region.getWhitelistedPlayers()) {
                        insertWhitelistStmt.setString(1, region.getName());
                        insertWhitelistStmt.setString(2, playerName);
                        insertWhitelistStmt.executeUpdate();
                    }
                }
            } catch (Exception e) {
                plugin.getComponentLogger().error("Couldn't save region to the database!", e);
            }
        });
    }


    public void updateRegionPos(String regionName, boolean isPos1, Location loc) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String pos = isPos1 ? "1" : "2";

            String sql = "UPDATE regions SET pos" + pos + "X = ?, pos" + pos + "Y = ?, pos" + pos + "Z = ?, pos" + pos + "World = ? WHERE name = ?";

            try (Connection connection = hikariDataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setDouble(1, loc.getX());
                statement.setDouble(2, loc.getY());
                statement.setDouble(3, loc.getZ());
                statement.setString(4, loc.getWorld().getName());
                statement.setString(5, regionName);

                statement.executeUpdate();
                ;
            } catch (Exception e) {
                plugin.getComponentLogger().error("Couldn't update region position!", e);
            }
        });
    }


    public void addPlayerToWhitelist(String regionName, String playerName) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "INSERT INTO region_whitelist (region_name, player_name) VALUES (?, ?)";

            try (Connection connection = hikariDataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, regionName);
                statement.setString(2, playerName);
                statement.executeUpdate();
            } catch (Exception e) {
                plugin.getComponentLogger().error("Couldn't add player to whitelist!", e);
            }
        });
    }


    public void removePlayerFromWhitelist(String regionName, String playerName) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "DELETE FROM region_whitelist WHERE region_name = ? AND player_name = ?";

            try (Connection connection = hikariDataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, regionName);
                statement.setString(2, playerName);
                statement.executeUpdate();
            } catch (Exception e) {
                plugin.getComponentLogger().error("Couldn't remove player from whitelist!", e);
            }
        });
    }


    public void addFlag(String regionName, Flag flag, FlagState flagState) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "INSERT INTO region_flags (region_name, flag, state) VALUES (?, ?, ?)";

            try (Connection connection = hikariDataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, regionName);
                statement.setString(2, flag.getName());
                statement.setString(3, flagState.name());

                statement.executeUpdate();
            } catch (Exception e) {
                plugin.getComponentLogger().error("Couldn't add or update flag!", e);
            }
        });
    }

    public void updateFlagState(String regionName, String flagName, FlagState state) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "UPDATE region_flags SET state = ? WHERE region_name = ? AND flag = ?";

            try (Connection connection = hikariDataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, state.name());
                statement.setString(2, regionName);
                statement.setString(3, flagName);
                statement.executeUpdate();
            } catch (Exception e) {
                plugin.getComponentLogger().error("Couldn't update flag state!", e);
            }
        });
    }


    public void updateRegionName(String oldRegionName, String newRegionName) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "UPDATE regions SET name = ? WHERE name = ?";

            try (Connection connection = hikariDataSource.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {

                statement.setString(1, newRegionName);
                statement.setString(2, oldRegionName);
                statement.executeUpdate();
            } catch (Exception e) {
                plugin.getComponentLogger().error("Couldn't update region name!", e);
            }
        });
    }


}
