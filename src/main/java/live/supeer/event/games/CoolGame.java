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

public class CoolGame extends Minigame implements Listener {

    // A template game for testing purposes

    public CoolGame(MinigameManager minigameManager) {
        super("CoolGame", Material.GOLD_INGOT, minigameManager);
    }

    @Override
    public List<GameMap> getAvailableMaps() {
        return Arrays.asList(
                new GameMap("testmap1", null, null, null, new Location(gameWorld, -4, 32, 0)),
                new GameMap("testmap2", null, null, null, new Location(gameWorld, -4, 32, 0))
        );
    }

    @Override
    public Location getLobbyLocation() {
        return new Location(gameWorld, 0, 100, 0); // Adjust coordinates accordingly
    }

    @Override
    public void startGame() {
        registerListeners();
        Bukkit.broadcastMessage("CoolGame has started!");
    }

    @Override
    public void endGame() {
        unregisterListeners();
        Bukkit.broadcastMessage("CoolGame has ended!");
        minigameManager.endGame();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.GOLD_BLOCK) {
            Player winner = event.getPlayer();
            Bukkit.broadcastMessage(winner.getName() + " has won the CoolGame!");
            endGame();
        }
    }
}
