package live.supeer.event;

import org.bukkit.entity.Player;

public class WaitingState implements GameState {
    private final MinigameManager minigameManager;

    public WaitingState(MinigameManager minigameManager) {
        this.minigameManager = minigameManager;
    }

    @Override
    public void start() {
        minigameManager.startVoting();
    }

    @Override
    public void stop() {
        // Clean up if needed
    }

    @Override
    public void reset() {
        // Reset the state, for example, clear votes or prepare the lobby
    }

    @Override
    public void handlePlayerJoin(Player player) {
        player.teleport(minigameManager.getLobbyLocation());
        player.sendMessage("Welcome to the lobby! Please wait for the next game.");
    }

    @Override
    public void handlePlayerLeave(Player player) {
        // Handle player leaving during the waiting state
    }
}

