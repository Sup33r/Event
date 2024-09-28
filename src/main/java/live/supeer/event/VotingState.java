package live.supeer.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class VotingState implements GameState {
    private final MinigameManager minigameManager;
    private final Map<Minigame, Integer> votes = new HashMap<>();

    public VotingState(MinigameManager minigameManager) {
        this.minigameManager = minigameManager;
    }

    @Override
    public void start() {
        Bukkit.broadcastMessage("Voting has started! Choosing the game in 10 seconds...");
        Bukkit.getScheduler().runTaskLater(Event.getInstance(), this::endVoting, 200L); // 10 seconds delay (200 ticks)
    }

    public void castVote(Player player, Minigame minigame) {
        votes.put(minigame, votes.getOrDefault(minigame, 0) + 1);
        player.sendMessage("You voted for " + minigame.getName());
    }

    public void endVoting() {
        Bukkit.broadcastMessage("Voting ended. The selected game is TestGame!");
        TestGame testGame = new TestGame(minigameManager);
        minigameManager.startMinigame(testGame);
    }

    @Override
    public void stop() {
        // Clean up vote data if needed
    }

    @Override
    public void reset() {
        votes.clear();
    }

    @Override
    public void handlePlayerJoin(Player player) {
        player.sendMessage("Voting is in progress. Use /vote <minigame> to participate!");
    }

    @Override
    public void handlePlayerLeave(Player player) {
        // Handle if players leave during voting
    }
}

