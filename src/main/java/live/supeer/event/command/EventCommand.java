package live.supeer.event.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Subcommand;
import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import com.github.stefvanschie.inventoryframework.pane.component.ToggleButton;
import live.supeer.event.Event;
import live.supeer.event.EventPlayer;
import live.supeer.event.managers.MinigameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
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
        StaticPane pane = new StaticPane(0, 0, 9, 2);

        EventPlayer eventPlayer = minigameManager.getOnlinePlayers().stream()
                .filter(ep -> ep.getUuid().equals(player.getUniqueId()))
                .findFirst()
                .orElse(null);

        if (eventPlayer == null) {
            player.sendMessage("Player data not found.");
            return;
        }

        ItemStack stateItem = new ItemStack(Material.PAPER);
        ItemMeta stateMeta = stateItem.getItemMeta();
        stateMeta.displayName(Component.text("Speldeltagande: " + (eventPlayer.isActive() ? "Aktiverat" : "Inaktiverat")).color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        stateMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        stateItem.setItemMeta(stateMeta);
        pane.addItem(new GuiItem(stateItem), 4, 0);

        ItemStack disabledItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta disabledMeta = disabledItem.getItemMeta();
        disabledMeta.displayName(Component.text("Inaktivt").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false));
        disabledItem.setItemMeta(disabledMeta);

        ItemStack enabledItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta enabledMeta = enabledItem.getItemMeta();
        enabledMeta.displayName(Component.text("Aktivt").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        enabledItem.setItemMeta(enabledMeta);

        ToggleButton toggleButton = new ToggleButton(4, 1, 1, 1);
        toggleButton.setDisabledItem(new GuiItem(disabledItem));
        toggleButton.setEnabledItem(new GuiItem(enabledItem));
        toggleButton.setOnClick(event -> {
            boolean newState = !eventPlayer.isActive();
            eventPlayer.setActive(newState);
            if (newState) {
                minigameManager.getActivePlayers().add(player);
            } else {
                minigameManager.getActivePlayers().remove(player);
            }
            stateMeta.displayName(Component.text("Speldeltagande: " + (newState ? "Aktiverat" : "Inaktiverat")).color(NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            stateItem.setItemMeta(stateMeta);
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

    @Subcommand("coins")
    public void onCoins(Player player) {
        EventPlayer eventPlayer = minigameManager.getEventPlayer(player);
        player.sendMessage("You have " + eventPlayer.getPoints() + " coins.");
    }

    @Subcommand("forcestart")
    @CommandCompletion("@minigames")
    public void onForceStart(Player player, String game) {
        minigameManager.prepareMinigame(minigameManager.getMinigame(game));
        player.sendMessage("Forcing game start...");
    }
}

