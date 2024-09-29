package live.supeer.event;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MinigameManager {
    private GameState currentState;
    private final WaitingState waitingState;
    private final VotingState votingState;
    private PlayingState playingState;
    private final EndingState endingState;
    private boolean gameInProgress = false;

    public MinigameManager() {
        this.waitingState = new WaitingState(this);
        this.votingState = new VotingState(this);
        this.playingState = null; // Will be set when a minigame starts
        this.endingState = new EndingState(this);

        currentState = waitingState; // Initial state
    }

    public void changeState(GameState newState) {
        if (currentState != null) {
            currentState.stop();
        }
        currentState = newState;
        currentState.start();
    }

    public void startVoting() {
        if (!gameInProgress) {
            changeState(votingState);
        } else {
            Bukkit.broadcastMessage("A game is already in progress. Please wait until it finishes.");
        }
    }

    public void startMinigame(Minigame minigame) {
        if (gameInProgress) {
            Bukkit.broadcastMessage("A game is already in progress.");
            return;
        }
        gameInProgress = true; // Set game in progress
        playingState = new PlayingState(this, minigame);
        changeState(playingState);
    }

    public void endGame() {
        changeState(endingState);
        gameInProgress = false; // Reset the flag when the game ends
    }

    public Location getLobbyLocation() {
        return Event.configuration.getLobbyLocation();
    }

    public void resetToLobby() {
        changeState(waitingState);
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

    public List<Minigame> getMinigames() {
        List<Minigame> minigames = new ArrayList<>();
        minigames.add(new CoolGame(this));
        minigames.add(new TestGame(this));
        return minigames;
    }
}

