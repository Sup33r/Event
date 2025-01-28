package live.supeer.event.games;

import live.supeer.event.*;
import live.supeer.event.managers.MinigameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.util.*;

public class TNTGame extends Minigame implements Listener {

    private List<Player> players = new ArrayList<>(minigameManager.getActivePlayers());
    private boolean gameEnded = false;
    private boolean gameStarted = false;

    private final List<Block> brokenBlocks = new ArrayList<>();

    private final ArrayList<Player> scores = new ArrayList<>();

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
        return new Location(gameWorld, -4, 38, 23);
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

        distributeCoins();

        Location center = currentMap.getCenterLocation();
        int radius = 17;
        int fireworkCount = 20;

        for (int i = 0; i < fireworkCount; i++) {
            double angle = 2 * Math.PI * i / fireworkCount;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            Location fireworkLocation = new Location(gameWorld, x, center.getY(), z);
            launchFirework(fireworkLocation);
        }

        Bukkit.getScheduler().runTaskLater(Event.getInstance(), () -> {
            resetPlayers();
            minigameManager.endGame();
        }, 200L);
    }

    public void distributeCoins() {
        Collections.reverse(scores);
        Map<Player, Integer> playerCoins = minigameManager.calculateCoins(scores, 1.0f);
        for (Map.Entry<Player, Integer> entry : playerCoins.entrySet()) {
            Player player = entry.getKey();
            int coins = entry.getValue();
            EventPlayer eventPlayer = minigameManager.getEventPlayer(player);
            eventPlayer.addPoints(coins);
            player.sendMessage("You have been awarded " + coins + " coins for participating in the event.");
        }
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
                () -> Event.broadcastMessage(players, "messages.games.common.starting"),
                () -> {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        final Component mainTitle = Component.text("KÖR!").color(TextColor.color(0xDCC6FF)).decorate(TextDecoration.BOLD);
                        final Component subTitle = Component.text("");
                        final Title.Times times = Title.Times.times(Duration.ofMillis(50), Duration.ofMillis(900), Duration.ofMillis(50));
                        final Title titleMessage = Title.title(mainTitle, subTitle, times);
                        player.showTitle(titleMessage);
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2.0f);
                    }
                    gameStarted = true;
                    startBlockRemoval();
                    giveItems();
                },
                (t) -> {
                    int secondsLeft = t.getSecondsLeft();
                    if (secondsLeft <= 5) {
                        String title = ">    " + secondsLeft + "    <";
                        String subtitle = Event.getMessage("messages.games.common.countdown.subtitle");
                        TextColor color;
                        TextColor subtitleColor = TextColor.color(0xD1CECF);
                        Sound sound = Sound.BLOCK_NOTE_BLOCK_HAT;
                        float pitch = 1.0f;

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
                                sound = Sound.BLOCK_NOTE_BLOCK_PLING;
                                break;
                            case 2:
                                color = TextColor.color(0xFFB300);
                                title = "> 2 <";
                                sound = Sound.BLOCK_NOTE_BLOCK_PLING;
                                break;
                            case 1:
                                color = TextColor.color(0xFF4B00);
                                title = ">1<";
                                sound = Sound.BLOCK_NOTE_BLOCK_PLING;
                                break;
                            default:
                                color = TextColor.color(0xDCC6FF);
                        }

                        for (Player player : Bukkit.getOnlinePlayers()) {
                            final Component mainTitle = Component.text(title).color(color).decorate(TextDecoration.BOLD);
                            assert subtitle != null;
                            final Component subTitle = Component.text(subtitle).color(subtitleColor);
                            final Title.Times times = Title.Times.times(Duration.ofMillis(50), Duration.ofMillis(900), Duration.ofMillis(50));
                            final Title titleMessage = Title.title(mainTitle, subTitle, times);

                            player.showTitle(titleMessage);

                            player.playSound(player.getLocation(), sound, 1, pitch);
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

                    brokenBlocks.add(block);

                    int blockId = block.getLocation().hashCode();

                    for (int i = 0; i <= 5; i++) {
                        final float damage = i / 5.0f; // Update less frequently
                        Bukkit.getScheduler().runTaskLater(Event.getInstance(), () -> {
                            for (Player p : players) {
                                p.sendBlockDamage(block.getLocation(), damage, blockId);
                            }
                        }, i * 4L);
                    }

                    Bukkit.getScheduler().runTaskLater(Event.getInstance(), () -> {
                        block.setType(Material.AIR);

                        gameWorld.spawnParticle(Particle.BLOCK, block.getLocation(), 10, block.getBlockData());

                        for (Player p : players) {
                            p.playSound(block.getLocation(), Sound.BLOCK_LODESTONE_BREAK, 0.3f, 1);
                        }
                    }, blockRemoveDelay + 20L);
                }
            }
        }, 0L, 5L);
    }

    private void launchFirework(Location location) {
        Firework firework = gameWorld.spawn(location, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        FireworkEffect.Type[] types = FireworkEffect.Type.values();
        FireworkEffect.Type type = types[new Random().nextInt(types.length)];
        Color[] colors = {Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.ORANGE, Color.PURPLE};
        Color color = colors[new Random().nextInt(colors.length)];
        Color fade = colors[new Random().nextInt(colors.length)];
        meta.addEffect(FireworkEffect.builder()
                .withColor(color)
                .withFade(fade)
                .with(type)
                .trail(true)
                .flicker(true)
                .build());
        meta.setPower(new Random().nextInt(2) + 1);
        firework.setFireworkMeta(meta);
    }

    private List<Block> getRemovableBlocks(Player player) {
        List<Block> removableBlocks = new ArrayList<>();
        Location playerLocation = player.getLocation();
        int SCAN_DEPTH = player.isOnGround() ? 2 : 6, y = playerLocation.getBlockY();

        Block block;

        for (int i = 0; i < SCAN_DEPTH; i++) {
            block = getBlockUnderPlayer(y--, playerLocation);

            if (block != null && block.getType() == currentMap.getTriggerBlock()) {
                if (brokenBlocks.contains(block)) {
                    continue;
                }
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
                Event.broadcastMessage(minigameManager.getOnlineBukkitPlayers(), "messages.games.tntgame.eliminated", "%player%", player.getName());
                enableSpectatorMode(player);
                players.remove(player);
                scores.add(player);
                if (players.size() == 1) {
                    Event.broadcastMessage(minigameManager.getOnlineBukkitPlayers(), "messages.games.common.winner", "%player%", players.getFirst().getName());
                    scores.add(players.getFirst());
                    //TODO: Printa ut statistik från spelet
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