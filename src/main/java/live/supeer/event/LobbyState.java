package live.supeer.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LobbyState implements GameState {
    private final MinigameManager minigameManager;
    private final Minigame minigame;

    public LobbyState(MinigameManager minigameManager, Minigame minigame) {
        this.minigameManager = minigameManager;
        this.minigame = minigame;
    }

    @Override
    public void start() {
        Bukkit.broadcastMessage("Game will start soon! Get ready.");
        teleportPlayersToLobby();
        Bukkit.getScheduler().runTaskLater(Event.getInstance(), () -> {
            minigameManager.startGameplay(minigame);
        }, 200L); // 10 seconds
    }

    @Override
    public void stop() {
        // Cleanup if necessary
    }

    @Override
    public void handlePlayerJoin(Player player) {
        player.sendMessage("Game is about to start! Teleporting you to the lobby.");
        player.teleport(minigame.getLobbyLocation());
    }

    @Override
    public void handlePlayerLeave(Player player) {
        // Handle player leaving during lobby state
    }

    private void teleportPlayersToLobby() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(minigame.getLobbyLocation());
        }
    }
}
