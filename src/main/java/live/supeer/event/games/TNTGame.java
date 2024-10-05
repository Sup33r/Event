package live.supeer.event.games;

import live.supeer.event.CountdownTimer;
import live.supeer.event.Event;
import live.supeer.event.GameMap;
import live.supeer.event.Minigame;
import live.supeer.event.managers.MinigameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.*;

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
                new GameMap("tntmap1", Material.PINK_CONCRETE, null, -22, new Location(gameWorld, -4, 32, 0)),
                new GameMap("tntmap2", Material.DIAMOND_BLOCK, null, -22, new Location(gameWorld, -4, 32, 0))
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
        registerListeners();
        spreadPlayers(currentMap.getCenterLocation(), 17, currentMap.getTriggerBlock(), 100);
        showCountdown();
    }

    @Override
    public void endGame() {
        if (gameEnded) return;
        gameEnded = true;
        unregisterListeners();
        blockRemovalTask.cancel();
        spectatorAll();
        Bukkit.getScheduler().runTaskLater(Event.getInstance(), () -> {
            resetPlayers();
            minigameManager.endGame();
            }, 200L);
    }

    public void spreadPlayers(Location center, int radius, Material triggerBlock, int maxAttempts) {
        for (Player player : players) {
            Location location = null;
            int attempts = 0;
            boolean validLocation = false;

            while (attempts < maxAttempts) {
                attempts++;
                double angle = Math.random() * 2 * Math.PI;
                double distance = Math.random() * radius;

                double xOffset = Math.cos(angle) * distance;
                double zOffset = Math.sin(angle) * distance;

                location = new Location(
                        gameWorld,
                        center.getX() + xOffset,
                        center.getY(),
                        center.getZ() + zOffset
                );

                location = gameWorld.getHighestBlockAt(location).getLocation();

                if (location.getBlock().getType() == triggerBlock) {
                    validLocation = true;
                    break;
                }
            }

            if (!validLocation) {
                location = currentMap.getCenterLocation();
            }

            location.add(0, 1, 0);

            player.teleport(location);
        }
    }

    public void giveItems() {
        for (Player player : players) {
            player.getInventory().clear();
            player.getInventory().addItem(new ItemStack(Material.FEATHER, 3));
        }
    }

    public void resetPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setAllowFlight(false);
            player.setFlying(false);
            player.setInvisible(false);
            player.setInvulnerable(false);
            player.getInventory().clear();
        }
    }

    public void spectatorAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            enableSpectatorMode(player);
        }
    }

    public void enableSpectatorMode(Player player) {
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setInvisible(true);
        player.setInvulnerable(true);
        player.teleport(new Location(gameWorld, 0, 100, 0));
        player.getInventory().clear();
    }

    public void showCountdown() {
        CountdownTimer timer = new CountdownTimer(
                10,
                () -> Bukkit.broadcastMessage("Game starting in 10 seconds!"),
                () -> {
                    Bukkit.broadcastMessage("Game starting now!");
                    gameStarted = true;
                    startBlockRemoval();
                    giveItems();
                },
                (t) -> {
                    int secondsLeft = t.getSecondsLeft();
                    Bukkit.broadcastMessage("Game starting in " + secondsLeft + " seconds!");

                    if (secondsLeft <= 5) {
                        String title = ">    " + secondsLeft + "    <";
                        String subtitle = "Starting game in..";
                        TextColor color;
                        TextColor subtitleColor = TextColor.color(0xD1CECF);

                        switch (secondsLeft) {
                            case 5:
                                color = TextColor.color(0xC600);
                                title = ">    5    <";
                                break;
                            case 4:
                                color = TextColor.color(0xFF00);
                                title = ">   4   <";
                                break;
                            case 3:
                                color = TextColor.color(0xF8FF00);
                                title = ">  3  <";
                                break;
                            case 2:
                                color = TextColor.color(0xFFB300);
                                title = "> 2 <";
                                break;
                            case 1:
                                color = TextColor.color(0xFF4B00);
                                title = ">1<";
                                break;
                            case 0:
                                color = TextColor.color(0xDCC6FF);
                                title = "GO!";
                                subtitle = "";
                                break;
                            default:
                                color = TextColor.color(0xDCC6FF);
                        }

                        for (Player player : Bukkit.getOnlinePlayers()) {
                            final Component mainTitle = Component.text(title).color(color).decorate(TextDecoration.BOLD);
                            final Component subTitle = Component.text(subtitle).color(subtitleColor);
                            final Title.Times times = Title.Times.times(Duration.ofMillis(50), Duration.ofMillis(900), Duration.ofMillis(50));
                            final Title titleMessage = Title.title(mainTitle, subTitle, times);
                            player.showTitle(titleMessage);
                        }
                    }
                }
        );
        timer.scheduleTimer();
    }

    private BukkitTask blockRemovalTask;

    public void startBlockRemoval() {
        final int blockRemoveDelay = 10;

        blockRemovalTask = Bukkit.getScheduler().runTaskTimer(Event.getInstance(), () -> {
            if (!gameStarted || gameEnded) return;

            for (Player player : players) {
                List<Block> blocksBelow = getRemovableBlocks(player);

                for (Block block : blocksBelow) {
                    if (!currentMap.getTriggerBlock().equals(block.getType())) continue;

                    Bukkit.getScheduler().runTaskLater(Event.getInstance(), () -> {
                        block.setType(Material.AIR);
                        gameWorld.spawnParticle(Particle.BLOCK, block.getLocation(), 1);
                        gameWorld.playSound(block.getLocation(), Sound.BLOCK_LODESTONE_BREAK, 0.3f, 1);
                    }, blockRemoveDelay);
                }
            }
        }, 0L, 5L);
    }

    private List<Block> getRemovableBlocks(Player player) {
        List<Block> removableBlocks = new ArrayList<>();
        Location playerLocation = player.getLocation();
        int SCAN_DEPTH = player.isOnGround() ? 2 : 6, y = playerLocation.getBlockY();

        Block block;

        for (int i = 0; i < SCAN_DEPTH; i++) {
            block = getBlockUnderPlayer(y--, playerLocation);

            if (block != null && block.getType() == currentMap.getTriggerBlock()) {
                removableBlocks.add(block);
            }
        }

        return removableBlocks;
    }

    private Block getBlockUnderPlayer(int y, Location location) {
        Position loc = new Position(location.getX(), y, location.getZ());
        Block b1 = loc.getBlock(location.getWorld(), 0.3, -0.3);

        if (b1.getType() != Material.AIR) {
            return b1;
        }

        Block b2 = loc.getBlock(location.getWorld(), -0.3, 0.3);

        if (b2.getType() != Material.AIR) {
            return b2;
        }

        Block b3 = loc.getBlock(location.getWorld(), 0.3, 0.3);

        if (b3.getType() != Material.AIR) {
            return b3;
        }

        Block b4 = loc.getBlock(location.getWorld(), -0.3, -0.3);

        if (b4.getType() != Material.AIR) {
            return b4;
        }

        return null;
    }

    private record Position(double x, int y, double z) {

        public Block getBlock(World world, double addx, double addz) {
            return world.getBlockAt(NumberConversions.floor(x + addx), y, NumberConversions.floor(z + addz));
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (gameEnded || !players.contains(player)) {
            return;
        }

        Location to = event.getTo().toBlockLocation();

        if (to.getBlockY() < currentMap.getElimHeight()) {
            if (gameStarted) {
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