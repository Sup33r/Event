package live.supeer.event.games;

import live.supeer.event.GameMap;
import live.supeer.event.Minigame;
import live.supeer.event.managers.MinigameManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Arrays;
import java.util.List;

public class TestGame extends Minigame implements Listener {

    private boolean gameEnded = false;

    public TestGame(MinigameManager minigameManager) {
        super("TestGame", Material.DIAMOND, minigameManager);
    }

    @Override
    public List<GameMap> getAvailableMaps() {
        return Arrays.asList(
                new GameMap("testmap1", null, null, null),
                new GameMap("testmap2", null, null, null)
        );
    }

    @Override
    public Location getLobbyLocation() {
        return new Location(gameWorld, 0, 100, 0); // Adjust coordinates accordingly
    }

    @Override
    public void startGame() {
        gameEnded = false;
        registerListeners();
        // Game logic here
        Bukkit.broadcastMessage("TestGame has started!");
    }

    @Override
    public void endGame() {
        if (gameEnded) return;
        gameEnded = true;
        unregisterListeners();
        Bukkit.broadcastMessage("TestGame has ended!");
        minigameManager.endGame();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Implement game logic, e.g., check for block interaction
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.DIAMOND_BLOCK) {
            Player winner = event.getPlayer();
            Bukkit.broadcastMessage(winner.getName() + " has won the TestGame!");
            endGame();
        }
    }
}


