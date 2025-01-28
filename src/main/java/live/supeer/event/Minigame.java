package live.supeer.event;

import live.supeer.event.managers.MinigameManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
    @Getter
    protected Material guiMaterial;

    private boolean listenersRegistered = false;

    public Minigame(String name, Material guiMaterial, MinigameManager minigameManager) {
        this.name = name;
        this.guiMaterial = guiMaterial;
        this.minigameManager = minigameManager;
    }

    public abstract List<GameMap> getAvailableMaps();

    public void prepareGame() {
        currentMap = selectMap();
        gameWorld = minigameManager.getMapManager().loadMap(currentMap);
    }

    protected GameMap selectMap() {
        List<GameMap> maps = getAvailableMaps();
        return maps.get(new Random().nextInt(maps.size()));
    }

    public abstract Location getLobbyLocation();

    public abstract void startGame();

    public abstract void endGame();

    public void registerListeners() {
        if (!listenersRegistered) {
            Bukkit.getPluginManager().registerEvents(this, Event.getInstance());
            listenersRegistered = true;
        }
    }

    public void unregisterListeners() {
        if (listenersRegistered) {
            HandlerList.unregisterAll(this);
            listenersRegistered = false;
        }
    }
}
