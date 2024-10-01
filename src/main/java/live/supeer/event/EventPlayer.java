package live.supeer.event;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@Getter
public class EventPlayer {
    private final UUID uuid;
    private final Player player;
    private int points;
    private boolean active;

    public EventPlayer(DbRow data) {
        this.uuid = UUID.fromString(data.getString("uuid"));
        this.points = data.getInt("points");
        this.active = data.get("active");
        this.player = Bukkit.getPlayer(this.uuid);
    }

    public void addPoints(int points) {
        this.points += points;
        DB.executeUpdateAsync("UPDATE `players` SET `points` = ? WHERE `uuid` = ?", this.points, this.uuid.toString());
    }

    public void setActive(boolean active) {
        this.active = active;
        DB.executeUpdateAsync("UPDATE `players` SET `active` = ? WHERE `uuid` = ?", this.active, this.uuid.toString());
    }
}
