package live.supeer.event.states;

import fr.mrmicky.fastboard.adventure.FastBoard;
import live.supeer.event.Event;
import live.supeer.event.managers.MinigameManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class EndingState implements GameState {
    private final MinigameManager minigameManager;

    public EndingState(MinigameManager minigameManager) {
        this.minigameManager = minigameManager;
    }

    @Override
    public void start() {
        Bukkit.broadcastMessage("Game over! Returning to lobby...");
        teleportPlayersToLobby();

        // Reset game and return to waiting state after a delay
        Bukkit.getScheduler().runTaskLater(Event.getInstance(), () -> {
            minigameManager.changeState(new WaitingState(minigameManager));
        }, 100L); // Delay in ticks
        updateScoreboard();
    }

    @Override
    public void stop() {
        // Cleanup if necessary
    }

    @Override
    public void handlePlayerJoin(Player player) {
        player.sendMessage("Game has just ended. Returning you to the lobby.");
        player.teleport(Event.configuration.getLobbyLocation());
        updateScoreboard();
    }

    @Override
    public void handlePlayerLeave(Player player) {
        // Handle player leaving during ending state
        updateScoreboard();
    }

    private void teleportPlayersToLobby() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(Event.configuration.getLobbyLocation());
            //TODO: Set the players gamemode to Survival, but use a custom implementation
            player.setGameMode(GameMode.ADVENTURE);
        }
    }

    private void updateScoreboard() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            FastBoard board = Event.playerBoards.get(player);
            board.updateLines(
                    Component.text(""),
                    Component.text("Spelare: " + Bukkit.getOnlinePlayers().size()),
                    Component.text("Status: Väntar"),
                    Component.text(""),
                    Component.text("enserver.se")
            );
        }
    }
}


