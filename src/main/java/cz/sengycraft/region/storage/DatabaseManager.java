package cz.sengycraft.region.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import cz.sengycraft.region.RegionPlugin;
import cz.sengycraft.region.configuration.ConfigurationManager;
import org.bukkit.configuration.ConfigurationSection;

import java.util.function.Consumer;

public class DatabaseManager {

    private RegionPlugin plugin;

    public void setPlugin(RegionPlugin plugin) {
        this.plugin = plugin;
    }

    private static DatabaseManager instance;

    public static DatabaseManager getInstance() {
        if (instance == null) instance = new DatabaseManager();
        return instance;
    }

    private HikariDataSource hikariDataSource;

    public void initializeDatabase(Consumer<Exception> callback) {
        try {
            hikariDataSource = new HikariDataSource(getHikariConfig());
        } catch (Exception e) {
            callback.accept(e);
        }
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
