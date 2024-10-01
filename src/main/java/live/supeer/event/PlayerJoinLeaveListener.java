package live.supeer.event;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinLeaveListener implements Listener {
    private final MinigameManager minigameManager;

    public PlayerJoinLeaveListener(MinigameManager minigameManager) {
        this.minigameManager = minigameManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        minigameManager.getCurrentState().handlePlayerJoin(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        minigameManager.getCurrentState().handlePlayerLeave(event.getPlayer());
    }
}

