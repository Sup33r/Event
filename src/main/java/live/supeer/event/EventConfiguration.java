package live.supeer.event;

import lombok.Getter;
import org.bukkit.Location;

import java.util.List;

@Getter
public class EventConfiguration {

    private final String sqlHost;
    private final int sqlPort;
    private final String sqlDatabase;
    private final String sqlUsername;
    private final String sqlPassword;

    private Location lobbyLocation;

    EventConfiguration(Event plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        sqlHost = plugin.getConfig().getString("sql.host");
        sqlPort = plugin.getConfig().getInt("sql.port");
        sqlDatabase = plugin.getConfig().getString("sql.database");
        sqlUsername = plugin.getConfig().getString("sql.username");
        sqlPassword = plugin.getConfig().getString("sql.password");

        lobbyLocation = plugin.getConfig().getLocation("lobbyLocation");

    }

    public void setLobbyLocation(Location location) {
        this.lobbyLocation = location;
        Event.getInstance().getConfig().set("lobbyLocation", location);
        Event.getInstance().saveConfig();
    }
}
