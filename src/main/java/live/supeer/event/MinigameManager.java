package live.supeer.event;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MinigameManager {
    private GameState currentState;
    private boolean gameInProgress = false;
    @Getter
    private final List<Minigame> minigames = new ArrayList<>();

    public MinigameManager() {
        loadMinigames();
        currentState = new WaitingState(this);
    }

    private void loadMinigames() {
        minigames.add(new TestGame(this));
        minigames.add(new CoolGame(this));
    }

    public void startVoting() {
        if (!gameInProgress) {
            changeState(new VotingState(this));
        } else {
            Bukkit.broadcastMessage("A game is already in progress. Please wait.");
        }
    }

    void changeState(GameState newState) {
        if (currentState != null) currentState.stop();
        currentState = newState;
        currentState.start();
    }

    public void startMinigame(Minigame minigame) {
        gameInProgress = true;
        changeState(new PlayingState(this, minigame));
    }

    public void endGame() {
        gameInProgress = false;
        changeState(new EndingState(this));
    }

    public Location getLobbyLocation() {
        return Event.configuration.getLobbyLocation();
    }

    public void resetToLobby() {
        changeState(new WaitingState(this));
    }

    public void handlePlayerJoin(Player player) {
        currentState.handlePlayerJoin(player);
    }

    public void handlePlayerLeave(Player player) {
        currentState.handlePlayerLeave(player);
    }

    public void teleportToLobby() {
        for (Player player : Event.getInstance().getServer().getOnlinePlayers()) {
            player.teleport(getLobbyLocation());
        }
    }

    public void teleportToMainGame() {
        for (Player player : Event.getInstance().getServer().getOnlinePlayers()) {
            player.teleport(new Location(player.getWorld(), 100, 100, 100));
        }
    }

}

