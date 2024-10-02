package live.supeer.event.states;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import live.supeer.event.Event;
import live.supeer.event.Minigame;
import live.supeer.event.managers.MinigameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        Bukkit.broadcastMessage("Voting has started! Please vote for the next minigame.");
        openVotingGUIForAllPlayers();
        // End voting after a set time
        Bukkit.getScheduler().runTaskLater(Event.getInstance(), this::endVoting, 600L); // 30 seconds
    }

    @Override
    public void stop() {
        votes.clear();
    }

    @Override
    public void handlePlayerJoin(Player player) {
        openVotingGUI(player);
    }

    @Override
    public void handlePlayerLeave(Player player) {
        // Remove player's vote if necessary
    }

    private void endVoting() {
        Minigame selectedMinigame = getMinigameWithMostVotes();
        Bukkit.broadcastMessage("The selected minigame is: " + selectedMinigame.getName());
        minigameManager.prepareMinigame(selectedMinigame);
    }

    private Minigame getMinigameWithMostVotes() {
        return votes.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(minigameManager.getMinigames().getFirst()); // Default to first minigame
    }

    private void openVotingGUIForAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            openVotingGUI(player);
        }
    }

    private void openVotingGUI(Player player) {
        ChestGui gui = new ChestGui(1, "Vote for a minigame");
        StaticPane pane = new StaticPane(0,0,9,1);
        int index = 0;
        for (Minigame minigame : minigameManager.getMinigames()) {
            ItemStack item = new ItemStack(minigame.getGuiMaterial());
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(minigame.getName()).color(NamedTextColor.YELLOW));
            item.setItemMeta(meta);
            GuiItem guiItem = new GuiItem(item, event -> {
                castVote(player, minigame);
                event.setCancelled(true);
                player.closeInventory();
            });
            pane.addItem(guiItem, index++, 0);
        }
        gui.addPane(pane);
        gui.setOnGlobalClick(event -> event.setCancelled(true));
        gui.show(player);
    }

    public void castVote(Player player, Minigame minigame) {
        votes.put(minigame, votes.getOrDefault(minigame, 0) + 1);
        player.sendMessage("You voted for " + minigame.getName());
    }
}


