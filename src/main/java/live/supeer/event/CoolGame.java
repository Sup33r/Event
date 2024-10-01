package live.supeer.event;

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

    public CoolGame(MinigameManager minigameManager) {
        super("CoolGame", minigameManager);
    }

    @Override
    public List<GameMap> getAvailableMaps() {
        return Arrays.asList(
                new GameMap("testmap1", null, null),
                new GameMap("testmap2", null, null)
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
        // Implement game logic, e.g., check for block interaction
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.GOLD_BLOCK) {
            Player winner = event.getPlayer();
            Bukkit.broadcastMessage(winner.getName() + " has won the CoolGame!");
            endGame();
        }
    }
}
