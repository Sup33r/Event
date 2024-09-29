package live.supeer.event;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import net.kyori.adventure.text.Component;
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
        Bukkit.broadcastMessage("Voting has started! You have 30 seconds to vote.");
        openVotingGUIForAllPlayers();

        Bukkit.getScheduler().runTaskLater(Event.getInstance(), this::endVoting, 600L);
    }

    @Override
    public void stop() {
        votes.clear();
    }

    @Override
    public void handlePlayerJoin(Player player) {
        openGui(player);
    }

    @Override
    public void handlePlayerLeave(Player player) {
        // Handle player leaving during voting
    }

    private void endVoting() {
        Minigame selectedMinigame = getMinigameWithMostVotes();
        Bukkit.broadcastMessage("The selected minigame is: " + selectedMinigame.getName() + " with " + votes.get(selectedMinigame) + " votes.");
        minigameManager.prepareMinigame(selectedMinigame);
    }

    public void castVote(Player player, Minigame minigame) {
        votes.put(minigame, votes.getOrDefault(minigame, 0) + 1);
        player.sendMessage("You voted for " + minigame.getName());
    }

    private Minigame getMinigameWithMostVotes() {
        return votes.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(minigameManager.getMinigames().getFirst());
    }

    private void openVotingGUIForAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            openGui(player);
        }
    }

    public void openGui(Player player) {
        ChestGui gui = new ChestGui(1, "Vote for a minigame");
        StaticPane pane = new StaticPane(0,0,9,1);
        int index = 0;
        for (Minigame minigame : minigameManager.getMinigames()) {
            ItemStack item = new ItemStack(Material.PINK_DYE);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(minigame.getName()));
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
}

