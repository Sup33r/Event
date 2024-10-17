package live.supeer.event.states;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import fr.mrmicky.fastboard.adventure.FastBoard;
import live.supeer.event.Event;
import live.supeer.event.Minigame;
import live.supeer.event.managers.MinigameManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class VotingState implements GameState {
    private final MinigameManager minigameManager;
    private final Map<Minigame, Integer> votes = new HashMap<>();

    private int remainingTime = 30;

    public VotingState(MinigameManager minigameManager) {
        this.minigameManager = minigameManager;
    }

    @Override
    public void start() {
        updateScoreboard();
        Bukkit.broadcastMessage("Voting has started! Please vote for the next minigame.");
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), "block.note_block.pling", 1, 1);
        }
        openVotingGUIForAllPlayers();
        // End voting after a set time
        new BukkitRunnable() {
            @Override
            public void run() {
                if (remainingTime > 0) {
                    remainingTime--;
                    updateScoreboard();
                } else {
                    endVoting();
                    this.cancel();
                }
            }
        }.runTaskTimer(Event.getInstance(), 0L, 20L); // 20 ticks = 1 second
    }

    @Override
    public void stop() {
        votes.clear();
    }

    @Override
    public void handlePlayerJoin(Player player) {
        openVotingGUI(player);
        updateScoreboard();
    }

    @Override
    public void handlePlayerLeave(Player player) {
        // Remove player's vote if necessary
        updateScoreboard();
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
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1, 2);
    }

    private void updateScoreboard() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            FastBoard board = Event.playerBoards.get(player);
            board.updateLines(
                    Component.text(""),
                    Component.text("Omröstning av spel"),
                    Component.text("Tid kvar: " + remainingTime + "s"),
                    Component.text(""),
                    Component.text("Spelare: " + Bukkit.getOnlinePlayers().size()),
                    Component.text(""),
                    Component.text("enserver.se")
            );
        }
    }
}


