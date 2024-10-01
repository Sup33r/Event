package live.supeer.event;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.List;

public class TNTGame extends Minigame implements Listener {

    public TNTGame(MinigameManager minigameManager) {
        super("TNTGame", minigameManager);
    }

    @Override
    public List<GameMap> getAvailableMaps() {
        return Arrays.asList(
                new GameMap("TNTMap1", "TNT", Arrays.asList("STONE", "DIRT")),
                new GameMap("TNTMap2", "TNT", Arrays.asList("GRASS_BLOCK", "SAND"))
        );
    }

    @Override
    public Location getLobbyLocation() {
        return new Location(gameWorld, 0, 100, 0); // Adjust coordinates accordingly
    }

    @Override
    public void startGame() {
        registerListeners();
        // Game logic here
        Bukkit.broadcastMessage("TestGame has started!");
    }

    @Override
    public void endGame() {
        // End the game
    }
}