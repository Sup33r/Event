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
        Bukkit.broadcastMessage("Game over! Returning to lobby...");
        minigameManager.teleportToLobby();

        // After returning to lobby, reset to waiting state
        Bukkit.getScheduler().runTaskLater(Event.getInstance(), () -> {
            minigameManager.changeState(new WaitingState(minigameManager));
        }, 100L); // 5 seconds
    }

    @Override
    public void stop() {
        // Clean up if needed
    }

    @Override
    public void handlePlayerJoin(Player player) {
        player.sendMessage("The game has ended!");
    }

    @Override
    public void handlePlayerLeave(Player player) {
        // Handle player leaving after game has ended
    }
}

