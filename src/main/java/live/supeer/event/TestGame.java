package live.supeer.event;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;

public class TestGame extends Minigame implements Listener {
    private final MinigameManager minigameManager;

    public TestGame(MinigameManager minigameManager) {
        super("TestGame");
        this.minigameManager = minigameManager;
        Bukkit.getPluginManager().registerEvents(this, Event.getInstance());
    }

    @Override
    public void startGame() {
        Bukkit.broadcastMessage("Teleporting all players to the TestGame lobby...");
        minigameManager.teleportToLobby();

        // Schedule teleport to the main game after 15 seconds
        Bukkit.getScheduler().runTaskLater(Event.getInstance(), () -> {
            Bukkit.broadcastMessage("Teleporting players to the main game area...");
            minigameManager.teleportToMainGame();
        }, 300L); // 15 seconds delay
    }

    @Override
    public void endGame() {
        Bukkit.broadcastMessage("Game has ended!");
    }

    @Override
    public void resetGame() {
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.DIAMOND_BLOCK) {
            Bukkit.broadcastMessage(event.getPlayer().getName() + " touched the diamond block and won the game!");

            // Teleport all players back to the lobby after 10 seconds
            Bukkit.getScheduler().runTaskLater(Event.getInstance(), () -> {
                Bukkit.broadcastMessage("Returning all players to the lobby...");
                minigameManager.resetToLobby();
            }, 200L); // 10 seconds delay

            // Clean up event listener
            PlayerInteractEvent.getHandlerList().unregister(this);
        }
    }
}

