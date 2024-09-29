package live.supeer.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class WaitingState implements GameState {
    private final MinigameManager minigameManager;

    public WaitingState(MinigameManager minigameManager) {
        this.minigameManager = minigameManager;
    }

    @Override
    public void start() {
        // Optionally, start the voting process automatically after a delay
    }

    @Override
    public void stop() {
        // Cleanup if necessary
    }

    @Override
    public void handlePlayerJoin(Player player) {
        player.sendMessage("You have joined the event!");
    }

    @Override
    public void handlePlayerLeave(Player player) {
        player.sendMessage("You have left the event!");
    }
}

