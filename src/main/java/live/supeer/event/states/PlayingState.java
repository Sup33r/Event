package live.supeer.event.states;

import live.supeer.event.Minigame;
import live.supeer.event.managers.MinigameManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class PlayingState implements GameState {
    private final MinigameManager minigameManager;
    private final Minigame minigame;

    public PlayingState(MinigameManager minigameManager, Minigame minigame) {
        this.minigameManager = minigameManager;
        this.minigame = minigame;
    }

    @Override
    public void start() {
        minigame.startGame();
    }

    @Override
    public void stop() {
        minigame.endGame();
    }

    @Override
    public void handlePlayerJoin(Player player) {
        player.sendMessage("A game is currently in progress. You will spectate.");
        //TODO: Dont use Spectator mode, but instead a custom implementation
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(minigame.getLobbyLocation());
    }

    @Override
    public void handlePlayerLeave(Player player) {
    }
}


