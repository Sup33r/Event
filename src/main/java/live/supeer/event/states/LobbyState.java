package live.supeer.event.states;

import fr.mrmicky.fastboard.adventure.FastBoard;
import live.supeer.event.*;
import live.supeer.event.managers.MinigameManager;
import net.kyori.adventure.text.Component;
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
        makeInvulnerable();
        updateScoreboard();
        teleportPlayersToLobby();
        announceNonActive();
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
        updateScoreboard();
        player.teleport(minigame.getLobbyLocation());
    }

    @Override
    public void handlePlayerLeave(Player player) {
        // Handle player leaving during lobby state
    }

    private void makeInvulnerable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setInvulnerable(true);
        }
    }

    private void teleportPlayersToLobby() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.teleport(minigame.getLobbyLocation());
        }
    }

    private void announceNonActive() {
        for (EventPlayer eventPlayer : minigameManager.getOnlinePlayers()) {
            if (!eventPlayer.isActive()) {
                eventPlayer.getPlayer().sendMessage("You are not active in the event. Change your settings to enable participation.");
            }
        }
    }

    private void updateScoreboard() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            FastBoard board = Event.playerBoards.get(player);
            board.updateLines(
                    Component.text(""),
                    Component.text("Spelare: " + Bukkit.getOnlinePlayers().size()),
                    Component.text("Spel: " + minigame.getName()),
                    Component.text(""),
                    Component.text("enserver.se")
            );
        }
    }
}
