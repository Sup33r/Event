package live.supeer.event.states;

import fr.mrmicky.fastboard.adventure.FastBoard;
import live.supeer.event.Event;
import live.supeer.event.managers.MinigameManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class WaitingState implements GameState {
    private final MinigameManager minigameManager;

    public WaitingState(MinigameManager minigameManager) {
        this.minigameManager = minigameManager;
    }

    @Override
    public void start() {
        Bukkit.broadcastMessage("Waiting for next game...");
        makeInvulnerable();
        updateScoreboard();
    }

    @Override
    public void stop() {
    }

    @Override
    public void handlePlayerJoin(Player player) {
        player.sendMessage("Welcome! Waiting for the next game.");
        player.teleport(Event.configuration.getLobbyLocation());
        updateScoreboard();
    }

    @Override
    public void handlePlayerLeave(Player player) {
        updateScoreboard();
    }

    private void makeInvulnerable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setInvulnerable(true);
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
                    Component.text("Event")
            );
        }
    }
}


