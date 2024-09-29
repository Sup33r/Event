package live.supeer.event;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class Minigame implements Listener {
    @Getter
    protected String name;
    protected final MinigameManager minigameManager;

    public Minigame(String name, MinigameManager minigameManager) {
        this.name = name;
        this.minigameManager = minigameManager;
    }

    public abstract void startGame();
    public abstract void endGame();

    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, Event.getInstance());
    }

    public void unregisterListeners() {
        HandlerList.unregisterAll(this);
    }
}
