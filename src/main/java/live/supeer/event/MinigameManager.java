package live.supeer.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class MinigameManager {
    private GameState currentState;
    private final WaitingState waitingState;
    private final VotingState votingState;
    private PlayingState playingState;
    private final EndingState endingState;

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
        changeState(votingState);
    }

    public void startMinigame(Minigame minigame) {
        playingState = new PlayingState(this, minigame);
        changeState(playingState);
    }

    public void endGame() {
        changeState(endingState);
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
}

