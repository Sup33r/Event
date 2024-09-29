package live.supeer.event;

import co.aikar.commands.PaperCommandManager;
import live.supeer.event.command.VoteCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public final class Event extends JavaPlugin {
    public Logger logger = null;
    public static EventConfiguration configuration;
    private static LanguageManager languageManager;

    private static Event plugin;
    private static MinigameManager minigameManager;

    public static Event getInstance() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;

        // Initialize configuration and language manager
        configuration = new EventConfiguration(this);
        languageManager = new LanguageManager(this, "sv_se");

        // Initialize MinigameManager
        minigameManager = new MinigameManager();

        // Register commands
        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(new VoteCommand(minigameManager));

        // Register player join/leave listener
        getServer().getPluginManager().registerEvents(new PlayerJoinLeaveListener(minigameManager), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static void sendMessage(@NotNull CommandSender sender, @NotNull String key, String... replacements) {
        String message = languageManager.getValue(key, getLocale(sender), replacements);
        if (message == null || message.isEmpty()) {
            sender.sendMessage("§cMessage not found: " + key);
        }
        if (message != null && !message.isEmpty()) {
            Component component = languageManager.getMiniMessage().deserialize(message);
            sender.sendMessage(component);
        }
    }

    public static String getMessage(@NotNull String key, String... replacements) {
        String message = languageManager.getValue(key, "sv_se", replacements);

        if (message != null && !message.isEmpty()) {
            // Deserialize MiniMessage to a Component
            Component component = languageManager.getMiniMessage().deserialize(message);
            // Convert the Component to a legacy formatted string
            return LegacyComponentSerializer.legacySection().serialize(component);
        }
        return null;
    }

    public static Component getMessageComponent(@NotNull String key, String... replacements) {
        String message = languageManager.getValue(key, "sv_se", replacements);

        if (message != null && !message.isEmpty()) {
            return languageManager.getMiniMessage().deserialize(message);
        }
        return null;
    }

    public static String getRawMessage(@NotNull String key, String... replacements) {
        return languageManager.getValue(key, "sv_se", replacements);
    }

    private static @NotNull String getLocale(@NotNull CommandSender sender) {
        if (sender instanceof Player) {
            return ((Player) sender).getLocale();
        } else {
            return Event.getInstance().getConfig().getString("settings.locale", "sv_se");
        }
    }
}
