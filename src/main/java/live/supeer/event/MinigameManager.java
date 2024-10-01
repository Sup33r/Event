package live.supeer.event;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MinigameManager {
    @Getter
    private GameState currentState;
    private boolean gameInProgress = false;
    @Getter
    private final MapManager mapManager;
    @Getter
    private final List<Minigame> minigames = new ArrayList<>();

    public MinigameManager() {
        this.mapManager = new MapManager();
        loadMinigames();
        currentState = new WaitingState(this);
        currentState.start();
    }

    private void loadMinigames() {
        minigames.add(new TestGame(this));
        minigames.add(new CoolGame(this));
        minigames.add(new TNTGame(this));
    }

    public void startVoting() {
        if (!gameInProgress) {
            changeState(new VotingState(this));
        } else {
            Bukkit.broadcastMessage("A game is already in progress. Please wait.");
        }
    }

    public void prepareMinigame(Minigame minigame) {
        gameInProgress = true;
        minigame.prepareGame();
        changeState(new LobbyState(this, minigame));
    }

    public void startGameplay(Minigame minigame) {
        changeState(new PlayingState(this, minigame));
    }

    public void endGame() {
        changeState(new EndingState(this));
        gameInProgress = false;
    }

    public void changeState(GameState newState) {
        if (currentState != null) {
            currentState.stop();
        }
        currentState = newState;
        currentState.start();
    }

    // Additional methods for handling player events
}
