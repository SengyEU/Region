package cz.sengycraft.region.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import cz.sengycraft.region.RegionPlugin;
import cz.sengycraft.region.configuration.ConfigurationManager;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.function.Consumer;

public class DatabaseManager {

    private RegionPlugin plugin;

    public void setPlugin(RegionPlugin plugin) {
        this.plugin = plugin;
    }

    public RegionPlugin getPlugin() {
        return plugin;
    }

    private static DatabaseManager instance;

    public static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    private HikariDataSource hikariDataSource;

    public HikariDataSource getHikariDataSource() {
        return hikariDataSource;
    }

    public void initializeDatabase(Consumer<Exception> callback) {
        try {
            hikariDataSource = new HikariDataSource(getHikariConfig());
            initializeTables();
        } catch (Exception e) {
            callback.accept(e);
        }
    }

    private void initializeTables() throws SQLException {
        Connection connection = hikariDataSource.getConnection();

        String createRegionsTable = """
                CREATE TABLE IF NOT EXISTS regions (
                    name VARCHAR(255) PRIMARY KEY,
                    pos1X DOUBLE,
                    pos1Y DOUBLE,
                    pos1Z DOUBLE,
                    pos1World VARCHAR(255),
                    pos2X DOUBLE,
                    pos2Y DOUBLE,
                    pos2Z DOUBLE,
                    pos2World VARCHAR(255)
                );
                """;

        String createRegionFlagsTable = """
            CREATE TABLE IF NOT EXISTS region_flags (
                region_name VARCHAR(255),
                flag VARCHAR(255),
                state VARCHAR(255),
                FOREIGN KEY (region_name) REFERENCES regions(name) ON DELETE CASCADE ON UPDATE CASCADE
            );
            """;

        String createRegionWhitelistTable = """
            CREATE TABLE IF NOT EXISTS region_whitelist (
                region_name VARCHAR(255),
                player_name VARCHAR(255),
                FOREIGN KEY (region_name) REFERENCES regions(name) ON DELETE CASCADE ON UPDATE CASCADE
            );
            """;


        PreparedStatement statement1 = connection.prepareStatement(createRegionsTable);
        PreparedStatement statement2 = connection.prepareStatement(createRegionFlagsTable);
        PreparedStatement statement3 = connection.prepareStatement(createRegionWhitelistTable);


        statement1.executeUpdate();
        statement2.executeUpdate();
        statement3.executeUpdate();

        connection.close();
    }


    private HikariConfig getHikariConfig() {

        HikariConfig hikariConfig = new HikariConfig();

        ConfigurationSection databaseConfig = ConfigurationManager.getInstance().getConfiguration("config").getConfigurationSection("database");

        String host = databaseConfig.getString("host");
        int port = databaseConfig.getInt("port");
        String database = databaseConfig.getString("database");
        String user = databaseConfig.getString("user");
        String password = databaseConfig.getString("password");
        boolean useSsl = databaseConfig.getBoolean("useSsl");

        ConfigurationSection propertiesConfig = databaseConfig.getConfigurationSection("properties");

        int connectionTimeout = propertiesConfig.getInt("connectionTimeout");
        int maxLifetime = propertiesConfig.getInt("maxLifetime");
        int maximumPoolSize = propertiesConfig.getInt("maximumPoolSize");
        int minimumIdle = propertiesConfig.getInt("minimumIdle");
        int keepaliveTime = propertiesConfig.getInt("keepaliveTime");
        boolean cachePrepStmts = propertiesConfig.getBoolean("cachePrepStmts");
        int prepStmtCacheSize = propertiesConfig.getInt("prepStmtCacheSize");
        int prepStmtCacheSqlLimit = propertiesConfig.getInt("prepStmtCacheSqlLimit");

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSsl;
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);

        hikariConfig.setConnectionTimeout(connectionTimeout);
        hikariConfig.setMaxLifetime(maxLifetime);
        hikariConfig.setMaximumPoolSize(maximumPoolSize);
        hikariConfig.setMinimumIdle(minimumIdle);
        hikariConfig.setKeepaliveTime(keepaliveTime);

        hikariConfig.addDataSourceProperty("cachePrepStmts", cachePrepStmts);
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", prepStmtCacheSize);
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", prepStmtCacheSqlLimit);

        return hikariConfig;
    }

    public void closeDatabase() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            if (hikariDataSource != null) hikariDataSource.close();
        });
    }

}
