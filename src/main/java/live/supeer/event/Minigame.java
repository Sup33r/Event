package live.supeer.event;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class Minigame implements Listener {
    @Getter
    protected String name;
    protected GameState gameState;

    public Minigame(String name) {
        this.name = name;
    }

    public abstract void startGame();
    public abstract void endGame();
    public abstract void resetGame();

    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, Event.getInstance());
    }

    public void unregisterListeners() {
        HandlerList.unregisterAll(this);
    }

    public void handlePlayerLeave(Player player) {
        gameState.handlePlayerLeave(player);
    }
}
