package live.supeer.event;

import co.aikar.commands.PaperCommandManager;
import co.aikar.idb.DB;
import live.supeer.event.command.EventCommand;
import live.supeer.event.listeners.PlayerJoinLeaveListener;
import live.supeer.event.managers.LanguageManager;
import live.supeer.event.managers.MinigameManager;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public final class Event extends JavaPlugin {
    @Getter
    private static Event instance;
    public static EventConfiguration configuration;
    public Logger logger = null;
    private static LanguageManager languageManager;

    @Override
    public void onEnable() {
        instance = this;
        configuration = new EventConfiguration(this);
        MinigameManager minigameManager = new MinigameManager();
        logger = getLogger();
        languageManager = new LanguageManager(this, "sv_se");

        Database.initialize();

        PaperCommandManager manager = new PaperCommandManager(this);
        manager.registerCommand(new EventCommand(minigameManager));

        getServer().getPluginManager().registerEvents(new PlayerJoinLeaveListener(minigameManager), this);
    }

    @Override
    public void onDisable() {
        DB.close();
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
