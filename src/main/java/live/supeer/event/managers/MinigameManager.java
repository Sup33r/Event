package live.supeer.event.managers;

import live.supeer.event.EventPlayer;
import live.supeer.event.Minigame;
import live.supeer.event.games.CoolGame;
import live.supeer.event.games.ParkourGame;
import live.supeer.event.games.TNTGame;
import live.supeer.event.games.TestGame;
import live.supeer.event.states.*;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinigameManager {
    @Getter
    private GameState currentState;
    private boolean gameInProgress = false;
    @Getter
    private final MapManager mapManager;
    @Getter
    private final List<Minigame> minigames = new ArrayList<>();

    @Getter
    @Setter
    protected List<Player> activePlayers = new ArrayList<>();

    @Getter
    protected List<EventPlayer> onlinePlayers = new ArrayList<>();

    @Getter
    protected List<Player> onlineBukkitPlayers = new ArrayList<>();

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
        minigames.add(new ParkourGame(this));
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

    public EventPlayer getEventPlayer(Player player) {
        return onlinePlayers.stream()
                .filter(eventPlayer -> eventPlayer.getPlayer().equals(player))
                .findFirst()
                .orElse(null);
    }

    public Map<Player, Integer> calculateCoins(ArrayList<Player> playerArrayList, float multiplier) {
        Map<Player, Integer> playerCoins = new HashMap<>();
        int totalPlayers = playerArrayList.size();
        int maxCoins = 100;
        int minCoins = 1;
        int poolSize = totalPlayers * 20;

        for (int i = 0; i < totalPlayers; i++) {
            Player player = playerArrayList.get(i);
            int baseCoins = poolSize - (i * (poolSize - minCoins) / (totalPlayers - 1));
            int awardedCoins = Math.round(baseCoins * multiplier);
            awardedCoins = Math.max(minCoins, Math.min(maxCoins, awardedCoins));
            playerCoins.put(player, awardedCoins);
        }
        return playerCoins;
    }

    public Map<Player, Integer> calculateCoins(Map<Player, Integer> playerScore, float multiplier) {
        Map<Player, Integer> playerCoins = new HashMap<>();
        List<Map.Entry<Player, Integer>> sortedEntries = new ArrayList<>(playerScore.entrySet());

        // Sort players by their scores in descending order
        sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        int totalPlayers = sortedEntries.size();
        int maxCoins = 100;
        int minCoins = 1;
        int poolSize = totalPlayers * 20;
        int rank = 0;
        int previousScore = Integer.MIN_VALUE;

        for (int i = 0; i < totalPlayers; i++) {
            Map.Entry<Player, Integer> entry = sortedEntries.get(i);
            Player player = entry.getKey();
            int score = entry.getValue();

            // Only increment rank if the score is different from the previous score
            if (score != previousScore) {
                rank = i + 1;
                previousScore = score;
            }

            int baseCoins = poolSize - ((rank - 1) * (poolSize - minCoins) / (totalPlayers - 1));
            int awardedCoins = Math.round(baseCoins * multiplier);
            awardedCoins = Math.max(minCoins, Math.min(maxCoins, awardedCoins));
            playerCoins.put(player, awardedCoins);
        }

        return playerCoins;
    }
        // Additional methods for handling player events
}
