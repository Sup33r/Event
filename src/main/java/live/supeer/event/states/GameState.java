package live.supeer.event.states;

import org.bukkit.entity.Player;

public interface    GameState {
    void start();
    void stop();
    void handlePlayerJoin(Player player);
    void handlePlayerLeave(Player player);
}