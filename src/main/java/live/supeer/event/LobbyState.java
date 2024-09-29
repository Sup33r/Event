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
        minigameManager.teleportPlayers(minigame.getLobbyLocation());
        Bukkit.getScheduler().runTaskLater(Event.getInstance(), () -> {
            minigameManager.startGameplay(minigame);
        }, 300L); // 15 seconds delay
    }

    @Override
    public void stop() {
        // Cleanup if necessary
    }

    @Override
    public void handlePlayerJoin(Player player) {
        player.sendMessage("You have joined the lobby for " + minigame.getName() + "!");
    }

    @Override
    public void handlePlayerLeave(Player player) {
        player.sendMessage("You have left the lobby for " + minigame.getName() + "!");
    }
}