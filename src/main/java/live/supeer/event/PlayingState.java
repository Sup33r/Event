package live.supeer.event;

import org.bukkit.entity.Player;

public class PlayingState implements GameState {
    private final MinigameManager minigameManager;
    private final Minigame currentMinigame;

    public PlayingState(MinigameManager minigameManager, Minigame currentMinigame) {
        this.minigameManager = minigameManager;
        this.currentMinigame = currentMinigame;
    }

    @Override
    public void start() {
        currentMinigame.startGame();
    }

    @Override
    public void stop() {
        currentMinigame.endGame();
    }

    @Override
    public void handlePlayerJoin(Player player) {
        player.sendMessage("The game has already started!");
    }

    @Override
    public void handlePlayerLeave(Player player) {
        // Handle player leaving during game
    }
}

