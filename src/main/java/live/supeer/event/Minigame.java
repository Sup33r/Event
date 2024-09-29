package live.supeer.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class Minigame implements Listener {
    @Getter
    protected String name;
    protected final MinigameManager minigameManager;
    @Setter
    @Getter
    protected boolean lobbyEnabled = true;

    public Minigame(String name, MinigameManager minigameManager) {
        this.name = name;
        this.minigameManager = minigameManager;
    }

    public abstract void startGame();
    public abstract void endGame();
    public abstract Location getLobbyLocation();

    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, Event.getInstance());
    }

    public void unregisterListeners() {
        HandlerList.unregisterAll(this);
    }
}
