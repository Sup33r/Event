package live.supeer.event.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Subcommand;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.component.ToggleButton;
import live.supeer.event.Event;
import live.supeer.event.EventPlayer;
import live.supeer.event.Minigame;
import live.supeer.event.MinigameManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        minigameManager.endGame();
    }

    @Subcommand("set")
    public class EventSet extends BaseCommand {

        @Subcommand("lobby")
        public void onLobby(Player player) {
            Event.configuration.setLobbyLocation(player.getLocation());
            player.sendMessage("Lobby location set!");
        }
    }

    @Subcommand("settings")
    public void onSettings(Player player) {
        showSettingsGui(player);
    }

    private void showSettingsGui(Player player) {
        ChestGui gui = new ChestGui(2, "Settings");
        StaticPane pane = new StaticPane(0, 0, 1, 1);

        // Get the EventPlayer instance for the player
        EventPlayer eventPlayer = minigameManager.getOnlinePlayers().stream()
                .filter(ep -> ep.getUuid().equals(player.getUniqueId()))
                .findFirst()
                .orElse(null);

        if (eventPlayer == null) {
            player.sendMessage("Player data not found.");
            return;
        }

        // Item to display the current active state
        ItemStack stateItem = new ItemStack(Material.PAPER);
        ItemMeta stateMeta = stateItem.getItemMeta();
        stateMeta.displayName(Component.text("Active State: " + (eventPlayer.isActive() ? "Active" : "Inactive")));
        stateItem.setItemMeta(stateMeta);
        pane.addItem(new GuiItem(stateItem), 4, 0);

        // Toggle button to switch the active state
        ToggleButton toggleButton = new ToggleButton(4, 1, 1, 1);
        toggleButton.setOnClick(event -> {
            boolean newState = !eventPlayer.isActive();
            eventPlayer.setActive(newState);
            stateMeta.displayName(Component.text("Active State: " + (newState ? "Active" : "Inactive")));
            stateItem.setItemMeta(stateMeta);
            player.sendMessage("Active state set to: " + (newState ? "Active" : "Inactive"));
        });

        if (eventPlayer.isActive() && !toggleButton.isEnabled()) {
            toggleButton.toggle();
        } else if (!eventPlayer.isActive() && toggleButton.isEnabled()) {
            toggleButton.toggle();
        }
        gui.addPane(toggleButton);
        gui.addPane(pane);
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        gui.show(player);
    }
}
