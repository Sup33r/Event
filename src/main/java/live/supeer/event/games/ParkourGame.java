package live.supeer.event.games;

import live.supeer.event.*;
import live.supeer.event.managers.MinigameManager;
import live.supeer.event.managers.SchematicManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.File;
import java.time.Duration;
import java.util.*;

public class ParkourGame extends Minigame implements Listener {

    private List<Player> players = new ArrayList<>(minigameManager.getActivePlayers());
    private boolean gameEnded = false;
    private boolean gameStarted = false;

    private final Map<Player, Integer> playerLevels = new HashMap<>();
    private final List<Location> checkpointLocations = new ArrayList<>();

    private final Location startingBlock = new Location(gameWorld, 6, 50, 3);

    private ItemDisplay barrierWall;
    private double barrierSpeed = 0.0005;
    private final double speedIncrement = 0.00008;

    private final SchematicManager schematicManager = new SchematicManager();

    private GameMap currentMap;

    public ParkourGame(MinigameManager minigameManager) {
        super("ParkourGame", Material.RABBIT_FOOT, minigameManager);
    }

    @Override
    public List<GameMap> getAvailableMaps() {
        return List.of(
                new GameMap("parkourmap1", null, null, 20, null)
        );
    }

    @Override
    public Location getLobbyLocation() {
        return new Location(gameWorld, -1, 51, -2);
    }

    @Override
    public void startGame() {
        resetPlayers();
        players = new ArrayList<>(minigameManager.getActivePlayers());
        gameStarted = false;
        gameEnded = false;
        registerListeners();
        currentMap = getAvailableMaps().getFirst();
        for (Player player : players) {
            playerLevels.put(player, 0);
        }
        generateParkourCourse();
        showCountdown();
    }

    @Override
    public void endGame() {
        if (gameEnded) return;
        gameEnded = true;
        unregisterListeners();
        
        Location playerLocation = players.getFirst().getLocation().clone().add(0, 5, 0);
        
        spectatorAll(playerLocation, players);
        
        sendScores();
        distributeCoins();

        int radius = 10;
        int fireworkCount = 20;

        for (int i = 0; i < fireworkCount; i++) {
            double angle = 2 * Math.PI * i / fireworkCount;
            double x = playerLocation.getX() + radius * Math.cos(angle);
            double z = playerLocation.getZ() + radius * Math.sin(angle);
            Location fireworkLocation = new Location(gameWorld, x, playerLocation.getY(), z);
            launchFirework(fireworkLocation);
        }

        Bukkit.getScheduler().runTaskLater(Event.getInstance(), () -> {
            resetPlayers();
            minigameManager.endGame();
        }, 500L);
    }

    public void resetPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setAllowFlight(false);
            player.setFlying(false);
            player.setCollidable(false);
            player.setInvisible(false);
            player.setInvulnerable(false);
            player.getInventory().clear();
        }
    }

    public void spectatorAll(Location location, List<Player> exclude) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!exclude.contains(player)) {
                enableSpectatorMode(player, location);
            }
        }
    }

    public void enableSpectatorMode(Player player, Location location) {
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setInvisible(true);
        player.setInvulnerable(true);
        player.teleport(location);
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
                    deleteWall();
                    spawnBarrierBlock();
                    gameStarted = true;
                },
                (t) -> {
                    int secondsLeft = t.getSecondsLeft();
                    if (secondsLeft <= 5) {
                        String title = ">    " + secondsLeft + "    <";
                        String subtitle = Event.getMessage("messages.games.common.countdown.subtitle");
                        TextColor color;
                        TextColor subtitleColor = TextColor.color(0xD1CECF);
                        Sound sound = Sound.BLOCK_NOTE_BLOCK_HAT; // Default sound for regular countdown steps
                        float pitch = 1.0f; // Default pitch

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

    private void spawnBarrierBlock() {
        Location spawnLocation = new Location(gameWorld, 0, 54, -10);
        barrierWall = gameWorld.spawn(spawnLocation, ItemDisplay.class, item -> {
            item.setItemStack(new ItemStack(Material.BARRIER));
            item.setCustomNameVisible(false);
            item.setGravity(false);
            item.setInvulnerable(true);
            item.setPersistent(true);
            item.setTransformation(new Transformation(
                    new Vector3f(0f, 0f, 0f), // Translation
                    new Quaternionf(0f, 0f, 0f, 1f), // Left rotation
                    new Vector3f(20f, 20f, 1f), // Scale
                    new Quaternionf(0f, 0f, 0f, 1f) // Right rotation
            ));
        });

        moveBarrierBlock();
    }

    private void moveBarrierBlock() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (gameEnded) {
                    this.cancel();
                    return;
                }

                Location currentLocation = barrierWall.getLocation();
                currentLocation.setZ(currentLocation.getZ() + barrierSpeed);
                barrierWall.teleport(currentLocation);

                barrierSpeed += speedIncrement; // Increase speed
                checkPlayerElimination();
            }
        }.runTaskTimer(Event.getInstance(), 0L, 1L); // Run every tick
    }

    private void checkPlayerElimination() {
        double barrierZ = barrierWall.getLocation().getZ();

        for (Player player : players) {
            if (player.getLocation().getZ() < barrierZ) {
                eliminatePlayer(player);
            }
        }
    }

    private void eliminatePlayer(Player player) {
        Event.broadcastMessage(minigameManager.getOnlineBukkitPlayers(), "messages.games.parkourgame.eliminated", "%player%", player.getName());
        enableSpectatorMode(player, player.getLocation().clone().add(0, 5, 0));
        players.remove(player);
        if (players.size() == 1) {
            Player winner = players.getFirst();
            Event.broadcastMessage(minigameManager.getOnlineBukkitPlayers(), "messages.games.parkourgame.winner", "%player%", winner.getName());
            endGame();
        }
    }

    private void deleteWall() {
        Location wallStart = new Location(gameWorld, 0, 51, 1);
        Location wallEnd = new Location(gameWorld, -2, 53, 1);
        for (int x = wallStart.getBlockX(); x >= wallEnd.getBlockX(); x--) {
            for (int y = wallStart.getBlockY(); y <= wallEnd.getBlockY(); y++) {
                for (int z = wallStart.getBlockZ(); z <= wallEnd.getBlockZ(); z++) {
                    Block block = gameWorld.getBlockAt(x, y, z);
                    block.setType(Material.AIR);
                }
            }
        }
    }

    private void distributeCoins() {
        Map<Player, Integer> playerCoins = minigameManager.calculateCoins(playerLevels, 1.0f);
        for (Map.Entry<Player, Integer> entry : playerCoins.entrySet()) {
            Player player = entry.getKey();
            int coins = entry.getValue();
            EventPlayer eventPlayer = minigameManager.getEventPlayer(player);
            eventPlayer.addPoints(coins);
            Event.sendMessage(player, "messages.games.parkourgame.winnings", "%points%", String.valueOf(coins), "%segments%", String.valueOf(playerLevels.get(player)));
        }
    }

    private void sendScores() {
        List<Player> topPlayers = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Player topPlayer = null;
            int topScore = 0;
            for (Player player : minigameManager.getActivePlayers()) {
                if (!playerLevels.containsKey(player)) {
                    continue;
                }
                int score = playerLevels.get(player);
                if (score > topScore && !topPlayers.contains(player)) {
                    topPlayer = player;
                    topScore = score;
                }
            }
            if (topPlayer != null) {
                topPlayers.add(topPlayer);
            }
        }

        Event.broadcastMessage(minigameManager.getOnlineBukkitPlayers(), "messages.games.common.line", "%color%", "gray");
        for (int i = 0; i < topPlayers.size(); i++) {
            Player player = topPlayers.get(i);
            Event.broadcastMessage(minigameManager.getOnlineBukkitPlayers(), "messages.games.parkourgame.top." + (i + 1), "%player%", player.getName(), "%score%", String.valueOf(playerLevels.get(player)));
        }
        Event.broadcastMessage(minigameManager.getOnlineBukkitPlayers(), "messages.games.common.line", "%color%", "gray");
    }

    private void generateParkourCourse() {
        List<File> schematicFiles = new ArrayList<>(schematicManager.listSchematicsForGame("parkourgame").keySet());
        int totalSchematics = 0;
        Location pastePosition = startingBlock.clone();
        int schematicIndex = 0;

        if (schematicFiles.isEmpty()) {
            Event.getInstance().getLogger().warning("No schematics found for parkourgame.");
            return;
        }

        Collections.shuffle(schematicFiles);

        checkpointLocations.clear();
        checkpointLocations.add(getLobbyLocation());

        int parkourLength = 10;
        int maxAttempts = parkourLength * 10;
        int attemptCount = 0;

        while (totalSchematics < parkourLength && attemptCount < maxAttempts) {
            attemptCount++;
            if (schematicIndex >= schematicFiles.size()) {
                Collections.shuffle(schematicFiles);
                schematicIndex = 0;
            }

            File schematicFile = schematicFiles.get(schematicIndex);
            schematicIndex++;
            // Load the schematic as Clipboard
            Clipboard clipboard = schematicManager.loadSchematic(schematicFile);
            if (clipboard == null) {
                Event.getInstance().getLogger().warning("Failed to load schematic: " + schematicFile.getName());
                continue;
            }

            List<BlockVector3> pastePositions = schematicManager.findLightBlocks(clipboard, 1);
            if (pastePositions.isEmpty()) {
                Event.getInstance().getLogger().warning("No level 1 light blocks found in schematic: " + schematicFile.getName());
                continue;
            }

            List<BlockVector3> checkpointPositions = schematicManager.findLightBlocks(clipboard, 0);

            if (checkpointPositions.isEmpty()) {
                Event.getInstance().getLogger().warning("No level 0 light blocks found in schematic: " + schematicFile.getName());
                continue;
            }

            // Get the actual local origin from schematic
            var offset = clipboard.getMinimumPoint().subtract(clipboard.getOrigin());

            // Transform position with offset
            BlockVector3 localPastePosition = pastePositions.getFirst().add(offset);
            BlockVector3 localCheckpointPosition = checkpointPositions.getFirst().add(offset);

            // Subtract the minimum point of the clipboard to get the local position
            localPastePosition = localPastePosition.subtract(clipboard.getMinimumPoint());
            localCheckpointPosition = localCheckpointPosition.subtract(clipboard.getMinimumPoint());

            schematicManager.pasteSchematic(gameWorld, clipboard, pastePosition);

            // Calculate the world coordinate of the checkpoint light block
            Location worldCheckpointPosition = pastePosition.clone().add(localCheckpointPosition.x(), localCheckpointPosition.y(), localCheckpointPosition.z());

            // Save the worldCheckpointPosition as a checkpoint
            worldCheckpointPosition.setWorld(gameWorld);
            checkpointLocations.add(worldCheckpointPosition);

            // Calculate the world coordinate of the next paste position
            pastePosition = pastePosition.clone().add(localPastePosition.x(), 0.0, localPastePosition.z());

            totalSchematics++;
        }

        // Load the finish schematic
        File finishSchematicFile = schematicManager.getGameSchematic("parkourgame", "parkourfinish.schem");
        Clipboard finishClipboard = schematicManager.loadSchematic(finishSchematicFile);
        if (finishClipboard == null) {
            Event.getInstance().getLogger().warning("Failed to load finish schematic: parkourfinish.schem");
            return;
        }

        List<BlockVector3> finishPositions = schematicManager.findLightBlocks(finishClipboard, 0);
        if (finishPositions.isEmpty()) {
            Event.getInstance().getLogger().warning("No level 0 light blocks found in finish schematic: parkourfinish.schem");
            return;
        }

        // Get the actual local origin from finish schematic
        var finishOffset = finishClipboard.getMinimumPoint().subtract(finishClipboard.getOrigin());

        // Transform position with offset
        BlockVector3 localFinishPosition = finishPositions.getFirst().add(finishOffset);

        // Subtract the minimum point of the clipboard to get the local position
        localFinishPosition = localFinishPosition.subtract(finishClipboard.getMinimumPoint());

        schematicManager.pasteSchematic(gameWorld, finishClipboard, pastePosition);

        // Calculate the world coordinate of the finish light block
        Location worldFinishPosition = pastePosition.clone().add(localFinishPosition.x(), localFinishPosition.y(), localFinishPosition.z());

        // Save the worldFinishPosition as the finish line location
        worldFinishPosition.setWorld(gameWorld);
        checkpointLocations.add(worldFinishPosition);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (gameEnded) {
            return;
        }
        Location to = event.getTo();

        if (to.getBlockY() < currentMap.getElimHeight()) {
            if (!gameStarted || !players.contains(player)) {
                player.teleport(getLobbyLocation());
                return;
            }
            int playerLevel = playerLevels.getOrDefault(player, 0);
            if (playerLevel < checkpointLocations.size()) {
                Location checkpoint = checkpointLocations.get(playerLevel);
                if (checkpoint.getWorld() == null) {
                    checkpoint.setWorld(gameWorld);
                }
                player.teleport(checkpoint);
                player.sendActionBar(Objects.requireNonNull(Event.getMessageComponent("messages.games.parkourgame.fell")));
            } else {
                player.teleport(getLobbyLocation());
            }
        } else {
            if (!players.contains(player)) {
                return;
            }
            int currentLevel = playerLevels.getOrDefault(player, 0);
            if (currentLevel < checkpointLocations.size() - 1) {
                Location nextCheckpoint = checkpointLocations.get(currentLevel + 1);
                if (nextCheckpoint.getWorld() == null) {
                    nextCheckpoint.setWorld(gameWorld);
                }

                if (to.getWorld().equals(nextCheckpoint.getWorld())) {
                    if (player.getLocation().getY() >= nextCheckpoint.getY() && player.getLocation().getZ() >= nextCheckpoint.getZ()) {
                        if (!gameStarted) {
                            player.sendMessage(":o Hur kom du hit?!");
                            player.teleport(getLobbyLocation());
                            return;
                        }
                        playerLevels.put(player, currentLevel + 1);
                        player.showTitle(Title.title(Component.text(""), Component.text("Checkpoint nådd!").color(NamedTextColor.YELLOW), Title.Times.times(Duration.ofMillis(50), Duration.ofMillis(900), Duration.ofMillis(50))));

                        if (currentLevel + 1 == checkpointLocations.size() - 1) {
                            Event.broadcastMessage(minigameManager.getOnlineBukkitPlayers(), "messages.games.parkourgame.finished", "%player%", player.getName());
                            endGame();
                        }
                    }
                }
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