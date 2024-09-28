package live.supeer.event;

import org.bukkit.entity.Player;

public interface GameState {
    void start();
    void stop();
    void reset();
    void handlePlayerJoin(Player player);
    void handlePlayerLeave(Player player);
}