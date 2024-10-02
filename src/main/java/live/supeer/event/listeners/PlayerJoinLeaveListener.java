package live.supeer.event.listeners;

import co.aikar.idb.DB;
import co.aikar.idb.DbRow;
import live.supeer.event.EventPlayer;
import live.supeer.event.managers.MinigameManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;

public class PlayerJoinLeaveListener implements Listener {
    private final MinigameManager minigameManager;

    public PlayerJoinLeaveListener(MinigameManager minigameManager) {
        this.minigameManager = minigameManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        handleEventPlayer(event.getPlayer());
        minigameManager.getCurrentState().handlePlayerJoin(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        minigameManager.getCurrentState().handlePlayerLeave(event.getPlayer());
    }

    public void handleEventPlayer(Player player) {
        try {
            DbRow row = DB.getFirstRow("SELECT * FROM `players` WHERE `uuid` = ?", player.getUniqueId().toString());
            if (row == null) {
                DB.executeInsert("INSERT INTO `players` (`uuid`, `points`, `active`) VALUES (?, ?, ?)",
                        player.getUniqueId().toString(),
                        0,
                        true);
                row = DB.getFirstRow("SELECT * FROM `players` WHERE `uuid` = ?", player.getUniqueId().toString());
            }
            EventPlayer eventPlayer = new EventPlayer(row);
            minigameManager.getOnlinePlayers().add(eventPlayer);
            if (eventPlayer.isActive()) {
                minigameManager.getActivePlayers().add(player);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

