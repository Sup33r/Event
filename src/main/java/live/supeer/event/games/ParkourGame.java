package live.supeer.event.games;

import live.supeer.event.*;
import live.supeer.event.managers.MinigameManager;
import live.supeer.event.managers.SchematicManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.meta.FireworkMeta;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.extent.clipboard.Clipboard;

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

    private final SchematicManager schematicManager = new SchematicManager();

    private GameMap currentMap;

    public ParkourGame(MinigameManager minigameManager) {
        super("ParkourGame", Material.RABBIT_FOOT, minigameManager);
    }

    @Override
    public List<GameMap> getAvailableMaps() {
        return List.of(
                new GameMap("parkourmap1", null, null, 0, null)
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
        spectatorAll();

        Location center = players.getFirst().getLocation();
        int radius = 10;
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

    private void generateParkourCourse() {
        // Initialize variables
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
            Event.getInstance().getLogger().info("Attempt " + attemptCount + " of " + maxAttempts);

            if (schematicIndex >= schematicFiles.size()) {
                Collections.shuffle(schematicFiles);
                schematicIndex = 0;
            }

            File schematicFile = schematicFiles.get(schematicIndex);
            schematicIndex++;
            Event.getInstance().getLogger().info("Loading schematic: " + schematicFile.getName());

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

            // Get the actual local orgin from schematic
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
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (gameEnded || !players.contains(player)) {
            return;
        }

        if (!gameStarted) {
            return;
        }

        Location to = event.getTo();

        if (to.getBlockY() < currentMap.getElimHeight()) {
            // Teleport to last checkpoint
            int playerLevel = playerLevels.getOrDefault(player, 0);
            if (playerLevel < checkpointLocations.size()) {
                Location checkpoint = checkpointLocations.get(playerLevel);
                if (checkpoint.getWorld() == null) {
                    checkpoint.setWorld(gameWorld);
                }
                player.teleport(checkpoint);
                player.sendMessage("You fell! Teleporting back to your last checkpoint.");
            } else {
                // If no checkpoint found, teleport to starting block
                player.teleport(startingBlock);
            }
        } else {
            // Check if player has passed the next checkpoint
            int currentLevel = playerLevels.getOrDefault(player, 0);
            if (currentLevel < checkpointLocations.size() - 1) {
                Location nextCheckpoint = checkpointLocations.get(currentLevel + 1);
                if (nextCheckpoint.getWorld() == null) {
                    nextCheckpoint.setWorld(gameWorld);
                }

                if (to.getWorld().equals(nextCheckpoint.getWorld())) {
                    if (player.getLocation().getY() >= nextCheckpoint.getY() && player.getLocation().getZ() >= nextCheckpoint.getZ()) {
                        playerLevels.put(player, currentLevel + 1);
                        player.sendMessage("Checkpoint reached!");
                    }
//                    double distanceSquared = to.distanceSquared(nextCheckpoint);
//                    Event.getInstance().getLogger().info("Distance squared to next checkpoint: " + distanceSquared);
//
//                    if (distanceSquared <= 9) { // Within 3 blocks
//                        playerLevels.put(player, currentLevel + 1);
//                        player.sendMessage("Checkpoint reached!");
//                    }
                }
            } else {
                player.sendMessage("You have completed the parkour!");
                endGame();
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