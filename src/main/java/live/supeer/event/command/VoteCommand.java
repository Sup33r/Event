package live.supeer.event.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import live.supeer.event.Event;
import live.supeer.event.MinigameManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("startevent")
public class VoteCommand extends BaseCommand {
    private final MinigameManager minigameManager;

    public VoteCommand(MinigameManager minigameManager) {
        this.minigameManager = minigameManager;
    }

    @CommandAlias("startvote")
    public void onStartVote(CommandSender sender) {
        sender.sendMessage("Starting the voting process...");
        minigameManager.startVoting();
    }

    @CommandAlias("lobby")
    public void onLobby(Player player) {
        Event.configuration.setLobbyLocation(player.getLocation());
        player.sendMessage("Lobby location set!");
    }
}
