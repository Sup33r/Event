package live.supeer.event;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Listener;

public class TNTGame extends Minigame implements Listener {

    public TNTGame(MinigameManager minigameManager) {
        super("TNTGame", minigameManager);
    }

    @Override
    public Location getLobbyLocation() {
        return new Location(Bukkit.getWorld("world"), -22, 153, -92);
    }

    @Override
    public void setLobbyEnabled(boolean lobbyEnabled) {
        super.setLobbyEnabled(true);
    }

    @Override
    public void startGame() {
        minigameManager.teleportPlayers(new Location(Bukkit.getWorld("world"), 0, 100, 0));
    }

    @Override
    public void endGame() {
        // End the game
    }
}
