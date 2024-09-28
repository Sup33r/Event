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
    public void reset() {
        currentMinigame.resetGame();
    }

    @Override
    public void handlePlayerJoin(Player player) {
        player.sendMessage("A game is currently in progress, you cannot join right now.");
    }

    @Override
    public void handlePlayerLeave(Player player) {
        currentMinigame.handlePlayerLeave(player);
    }
}

