package live.supeer.event;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;

public class TNTGame extends Minigame implements Listener {

    private final List<Player> players = minigameManager.activePlayers;

    public TNTGame(MinigameManager minigameManager) {
        super("TNTGame", minigameManager);
    }

    @Override
    public List<GameMap> getAvailableMaps() {
        return Arrays.asList(
                new GameMap("TNTMap1", Material.PINK_CONCRETE, null, 30),
                new GameMap("TNTMap2", Material.DIAMOND_BLOCK, null, 30)
        );
    }

    @Override
    public Location getLobbyLocation() {
        return new Location(gameWorld, 0, 100, 0); // Adjust coordinates accordingly
    }

    @Override
    public void startGame() {
        registerListeners();
        // Spread players
        // Start countdown
        // Start removing blocks, allow feathers, register deaths
        Bukkit.broadcastMessage("TestGame has started!");
    }

    @Override
    public void endGame() {
        // End the game
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom().toBlockLocation();
        Location to = event.getTo().toBlockLocation();
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        if (to.getBlock().getType().equals(currentMap.getTriggerBlock())) {
            // TODO: Display mining animation on the block.
            Bukkit.getScheduler().runTaskLater(Event.getInstance(), () -> {
                to.getBlock().setType(Material.AIR);
            }, 20L);
        }

        if (to.getBlockY() < currentMap.getElimHeight()) {
            Bukkit.broadcastMessage(player.getName() + " has been eliminated!");
            players.remove(player);
            if (players.size() == 1) {
                Bukkit.broadcastMessage(players.getFirst().getName() + " has won the TNTGame!");
                endGame();
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            if (event.getItem() != null && event.getItem().getType() == Material.FEATHER) {
                Vector vector = new Vector(0, 1.65, 0);
                event.getPlayer().setVelocity(vector);
                event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
            }
        }
    }
}