package live.supeer.event.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import live.supeer.event.Event;
import live.supeer.event.MinigameManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("event")
public class EventCommand extends BaseCommand {
    private final MinigameManager minigameManager;

    public EventCommand(MinigameManager minigameManager) {
        this.minigameManager = minigameManager;
    }

    @Subcommand("start")
    public void onStart(CommandSender sender) {
        sender.sendMessage("Starting the voting process...");
        minigameManager.startVoting();
    }

    @Subcommand("stop")
    public void onStop(CommandSender sender) {
        sender.sendMessage("Stopping the game...");
        minigameManager.stopGame();
    }

    @Subcommand("set")
    public class EventSet extends BaseCommand {

        @Subcommand("lobby")
        public void onLobby(Player player) {
            Event.configuration.setLobbyLocation(player.getLocation());
            player.sendMessage("Lobby location set!");
        }
    }
}
