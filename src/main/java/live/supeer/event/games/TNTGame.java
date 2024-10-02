package live.supeer.event.games;

import live.supeer.event.CountdownTimer;
import live.supeer.event.Event;
import live.supeer.event.GameMap;
import live.supeer.event.Minigame;
import live.supeer.event.managers.MinigameManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TNTGame extends Minigame implements Listener {

    private List<Player> players = new ArrayList<>(minigameManager.getActivePlayers());
    private boolean gameEnded = false;

    private boolean gameStarted = false;

    public TNTGame(MinigameManager minigameManager) {
        super("TNTGame", Material.TNT, minigameManager);
    }

    @Override
    public List<GameMap> getAvailableMaps() {
        return Arrays.asList(
                new GameMap("tntmap1", Material.PINK_CONCRETE, null, -22),
                new GameMap("tntmap2", Material.DIAMOND_BLOCK, null, -22)
        );
    }

    @Override
    public Location getLobbyLocation() {
        return new Location(gameWorld, -4, 38, 23); // Adjust coordinates accordingly
    }

    @Override
    public void startGame() {
        resetPlayers();
        players = new ArrayList<>(minigameManager.getActivePlayers());
        gameStarted = false;
        gameEnded = false;
        Bukkit.broadcastMessage(minigameManager.getActivePlayers().stream().map(Player::getName).reduce("Players: ", (a, b) -> a + ", " + b));
        Bukkit.broadcastMessage(currentMap.getWorldName());
        Bukkit.broadcastMessage(currentMap.getElimHeight().toString());
        Bukkit.broadcastMessage(currentMap.getTriggerBlock().toString());
        registerListeners();
        spreadPlayers();
        showCountdown();
        // Spread players
        // Start countdown
        // Start removing blocks, allow feathers, register deaths
    }

    @Override
    public void endGame() {
        if (gameEnded) return;
        gameEnded = true;
        unregisterListeners();
        minigameManager.endGame();
    }

    public void spreadPlayers() {
        for (Player player : players) {
            Location location;
            int attempts = 0;
            do {
                attempts++;
                // Get a random location within 16 blocks of -4, 32, 0
                location = new Location(gameWorld, -4 + (Math.random() * 32 - 16), 100, 32 + (Math.random() * 32 - 16));
                // Get the highest block at the location
                location = gameWorld.getHighestBlockAt(location).getLocation();
                Bukkit.broadcastMessage("Attempt " + attempts + ": Checking location " + location);
            } while ((location.getBlockY() == 0 || location.getBlock().getType() != currentMap.getTriggerBlock()) && attempts < 100); // Ensure the block is the trigger block

            if (attempts >= 100) {
                player.sendMessage("Failed to find a valid location after 100 attempts.");
                continue;
            }

            // Place the player one block above the highest block found
            location.add(0, 1, 0);

            // Teleport the player to the location
            player.teleport(location);
            Bukkit.broadcastMessage("Player " + player.getName() + " teleported to " + location);
        }
        Bukkit.broadcastMessage("All players have been teleported!");
    }

    public void giveItems() {
        for (Player player : players) {
            player.getInventory().clear();
            player.getInventory().addItem(new ItemStack(Material.FEATHER, 3));
        }
        Bukkit.broadcastMessage("All players have been given items!");
    }

    public void resetPlayers() {
        for (Player player : players) {
            player.setAllowFlight(false);
            player.setFlying(false);
            player.setInvisible(false);
            player.setInvulnerable(false);
            player.getInventory().clear();
        }
    }

    public void enableSpectatorMode(Player player) {
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setInvisible(true);
        player.setInvulnerable(true);
        player.teleport(new Location(gameWorld, 0, 100, 0));
    }

    public void showCountdown() {
        CountdownTimer timer = new CountdownTimer(
                10,
                () -> Bukkit.broadcastMessage("Game starting in 10 seconds!"),
                () -> {
                    Bukkit.broadcastMessage("Game starting now!");
                    gameStarted = true;
                    giveItems();
                },
                (t) -> Bukkit.broadcastMessage("Game starting in " + t.getSecondsLeft() + " seconds!")
        );
        timer.scheduleTimer();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom().toBlockLocation();
        Location to = event.getTo().toBlockLocation();

        if (!gameStarted && players.contains(player)) {
            if (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                event.setCancelled(true);
            }
        }

        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        if (gameStarted && players.contains(player)) {
            Location blockBelow = new Location(gameWorld, to.getBlockX(), to.getBlockY() - 1, to.getBlockZ());
            if (blockBelow.getBlock().getType() == currentMap.getTriggerBlock()) {
                // Display mining animation on the block (optional)
                Bukkit.getScheduler().runTaskLater(Event.getInstance(), () -> {
                    blockBelow.getBlock().setType(Material.AIR);
                    Bukkit.broadcastMessage("Block at " + blockBelow + " set to AIR");
                }, 20L);
            }
        }

        if (to.getBlockY() < currentMap.getElimHeight()) {
            if (gameStarted && players.contains(player)) {
                Bukkit.broadcastMessage(player.getName() + " has been eliminated!");
                enableSpectatorMode(player);
                players.remove(player);
                if (players.size() == 1) {
                    Bukkit.broadcastMessage(players.getFirst().getName() + " has won the TNTGame!");
                    endGame();
                }
            } else {
                player.teleport(getLobbyLocation());
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!gameStarted) {
            event.setCancelled(true);
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            if (event.getItem() != null && event.getItem().getType() == Material.FEATHER) {
                Vector vector = new Vector(0, 1.65, 0);
                event.getPlayer().setVelocity(vector);
                event.getPlayer().getInventory().getItemInMainHand().setAmount(event.getPlayer().getInventory().getItemInMainHand().getAmount() - 1);
            }
        }
    }

    @EventHandler
    public void onPlayerTakeDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
        }
    }
}