package live.supeer.event.states;

import live.supeer.event.Event;
import live.supeer.event.managers.MinigameManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class WaitingState implements GameState {
    private final MinigameManager minigameManager;

    public WaitingState(MinigameManager minigameManager) {
        this.minigameManager = minigameManager;
    }

    @Override
    public void start() {
        Bukkit.broadcastMessage("Waiting for players...");
    }

    @Override
    public void stop() {
        // Cleanup if necessary
    }

    @Override
    public void handlePlayerJoin(Player player) {
        player.sendMessage("Welcome! Waiting for the next game.");
        player.teleport(Event.configuration.getLobbyLocation());
    }

    @Override
    public void handlePlayerLeave(Player player) {
        // Handle player leaving during waiting state
    }
}


