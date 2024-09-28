package live.supeer.event;

import lombok.Getter;
import org.bukkit.entity.Player;

public abstract class Minigame {
    @Getter
    protected String name;
    protected GameState gameState;

    public Minigame(String name) {
        this.name = name;
    }

    public abstract void startGame();
    public abstract void endGame();
    public abstract void resetGame();

    public void handlePlayerLeave(Player player) {
        gameState.handlePlayerLeave(player);
    }
}

