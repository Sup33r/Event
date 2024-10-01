package live.supeer.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Random;

public abstract class Minigame implements Listener {
    @Getter
    protected final String name;
    protected final MinigameManager minigameManager;
    protected GameMap currentMap;
    protected World gameWorld;

    public Minigame(String name, MinigameManager minigameManager) {
        this.name = name;
        this.minigameManager = minigameManager;
    }

    // Get a list of available maps for this minigame
    public abstract List<GameMap> getAvailableMaps();

    // Prepare the game (load map, setup environment)
    public void prepareGame() {
        currentMap = selectMap();
        gameWorld = minigameManager.getMapManager().loadMap(currentMap);
    }

    // Select a map (could be random or based on criteria)
    protected GameMap selectMap() {
        List<GameMap> maps = getAvailableMaps();
        // You can implement a map voting system here if desired
        return maps.get(new Random().nextInt(maps.size()));
    }

    // Get the lobby location for the minigame
    public abstract Location getLobbyLocation();

    // Start the game logic
    public abstract void startGame();

    // End the game logic
    public abstract void endGame();

    // Register and unregister event listeners
    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, Event.getInstance());
    }

    public void unregisterListeners() {
        HandlerList.unregisterAll(this);
    }
}
