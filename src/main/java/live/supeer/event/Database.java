package live.supeer.event;

import co.aikar.idb.BukkitDB;
import co.aikar.idb.DB;

import java.sql.SQLException;

public class Database {

    public static void initialize() {
        try {
            BukkitDB.createHikariDatabase(
                    Event.getInstance(),
                    Event.configuration.getSqlUsername(),
                    Event.configuration.getSqlPassword(),
                    Event.configuration.getSqlDatabase(),
                    Event.configuration.getSqlHost()
                            + ":"
                            + Event.configuration.getSqlPort());
            createTables();
        } catch (Exception e) {
            Event.getInstance().getLogger().warning("Failed to initialize database, disabling plugin.");
            Event.getInstance().getServer().getPluginManager().disablePlugin(Event.getInstance());
        }
    }

    public static void createTables() {
        try {
            DB.executeUpdate(
                    """
                              CREATE TABLE IF NOT EXISTS `placeholder` (
                                `plotId` int(11) NOT NULL,
                                `playerUUID` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
                                `count` bigint(30) DEFAULT NULL,
                                 PRIMARY KEY (plotId,playerUUID)
                              ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;""");
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
