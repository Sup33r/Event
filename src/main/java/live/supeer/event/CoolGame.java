package live.supeer.event;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class CoolGame extends Minigame implements Listener {

    public CoolGame(MinigameManager minigameManager) {
        super("CoolGame", minigameManager);
    }

    @Override
    public void startGame() {
        registerListeners();
        Bukkit.broadcastMessage("Starting CoolGame...");
        minigameManager.teleportToLobby();

        // Schedule teleport to main game area after 15 seconds
        Bukkit.getScheduler().runTaskLater(Event.getInstance(), () -> {
            Bukkit.broadcastMessage("Teleporting to the game area...");
            minigameManager.teleportToMainGame();
        }, 300L); // 15 seconds
    }

    @Override
    public void endGame() {
        Bukkit.broadcastMessage("CoolGame has ended.");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.GOLD_BLOCK) {
            Player winner = event.getPlayer();
            Bukkit.broadcastMessage(winner.getName() + " has won the CoolGame!");
            unregisterListeners();

            // End the game after 10 seconds
            Bukkit.getScheduler().runTaskLater(Event.getInstance(), () -> {
                minigameManager.endGame();
            }, 200L); // 10 seconds
        }
    }
}
