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
        for (Player player : Bukkit.getOnlinePlayers()) {
            openGui(player);
        }
        Bukkit.getScheduler().runTaskLater(Event.getInstance(), this::endVoting, 200L); // 10 seconds delay (200 ticks)
    }

    public void castVote(Player player, Minigame minigame) {
        votes.put(minigame, votes.getOrDefault(minigame, 0) + 1);
        player.sendMessage("You voted for " + minigame.getName());
    }

    public void endVoting() {
        Minigame minigame = votes.entrySet().stream().max(Comparator.comparingInt(Map.Entry::getValue)).map(Map.Entry::getKey).orElse(null);
        if (minigame == null) {
            Bukkit.broadcastMessage("No votes were cast. Choosing a random minigame...");
            minigame = minigameManager.getMinigames().get((int) (Math.random() * minigameManager.getMinigames().size()));
        }
        Bukkit.broadcastMessage("The minigame " + minigame.getName() + " has been chosen!");
        minigameManager.startMinigame(minigame);
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

