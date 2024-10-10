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
            Event.getInstance().getLogger().warning("Failed to initialize database, disabling plugin." + e.getMessage());
            Event.getInstance().getServer().getPluginManager().disablePlugin(Event.getInstance());
        }
    }

    public static void createTables() {
        try {
            DB.executeUpdate(
                    """
                              CREATE TABLE IF NOT EXISTS `players` (
                                `uuid` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
                                `points` bigint(30) DEFAULT '0',
                                `active` tinyint(1) DEFAULT '0',
                                 PRIMARY KEY (uuid)
                              ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;""");
            DB.executeUpdate(
                    """
                              CREATE TABLE IF NOT EXISTS `games` (
                                `id` int(11) NOT NULL AUTO_INCREMENT,
                                `game` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
                                `type` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                `multiplier` int(11) DEFAULT '1',
                                `scores` text COLLATE utf8mb4_unicode_ci NOT NULL,
                                `data` text COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                 PRIMARY KEY (id)
                              ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;""");
        } catch (SQLException exception) {
            throw new RuntimeException("Failed to create tables exception: " + exception.getMessage());
        }
    }
}
