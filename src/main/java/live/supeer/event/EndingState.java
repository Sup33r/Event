package live.supeer.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class EndingState implements GameState {
    private final MinigameManager minigameManager;

    public EndingState(MinigameManager minigameManager) {
        this.minigameManager = minigameManager;
    }

    @Override
    public void start() {
        // Teleport all players back to the lobby
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(minigameManager.getLobbyLocation());
            player.sendMessage("Game over! Returning to the lobby...");
        }
        // Reset minigame state and prepare for the next game
        minigameManager.resetToLobby();
    }

    @Override
    public void stop() {
        // Clean up if needed
    }

    @Override
    public void reset() {
        // Reset relevant data if needed
    }

    @Override
    public void handlePlayerJoin(Player player) {
        // Handle players who join while in the ending phase
    }

    @Override
    public void handlePlayerLeave(Player player) {
        // Handle players leaving while in the ending phase
    }
}

