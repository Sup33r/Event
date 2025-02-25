package live.supeer.event.managers;

import live.supeer.event.Event;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class LanguageManager {

    private final Event plugin;
    private final String defaultLocale;
    private final Map<String, YamlConfiguration> locales;
    @Getter
    private final MiniMessage miniMessage;

    public LanguageManager(@NotNull Event plugin, @NotNull String defaultLocale) {
        this.plugin = plugin;
        this.defaultLocale = defaultLocale;
        this.locales = new HashMap<>();
        this.miniMessage = MiniMessage.miniMessage();
        getOrLoadLocale(defaultLocale);
    }

    private YamlConfiguration getOrLoadLocale(@NotNull String locale) {
        YamlConfiguration loaded = locales.get(locale);
        if (loaded != null) {
            return loaded;
        }

        InputStream resourceStream = plugin.getResource(locale + ".yml");
        YamlConfiguration localeConfigDefaults;
        if (resourceStream == null) {
            localeConfigDefaults = new YamlConfiguration();
        } else {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceStream))) {
                localeConfigDefaults = YamlConfiguration.loadConfiguration(reader);
            } catch (IOException e) {
                plugin
                        .getLogger()
                        .log(Level.WARNING, "Unable to load resource " + locale + ".yml", e);
                localeConfigDefaults = new YamlConfiguration();
            }
        }

        File file = new File(plugin.getDataFolder(), locale + ".yml");
        YamlConfiguration localeConfig;

        if (!file.exists()) {
            localeConfig = localeConfigDefaults;
            try {
                localeConfigDefaults.save(file);
            } catch (IOException e) {
                plugin
                        .getLogger()
                        .log(Level.WARNING, "Unable to save resource " + locale + ".yml", e);
            }
        } else {
            localeConfig = YamlConfiguration.loadConfiguration(file);

            // Add new language keys
            List<String> newKeys = new ArrayList<>();
            for (String key : localeConfigDefaults.getKeys(true)) {
                if (localeConfigDefaults.isConfigurationSection(key)) {
                    continue;
                }

                if (localeConfig.isSet(key)) {
                    continue;
                }

                localeConfig.set(key, localeConfigDefaults.get(key));
                newKeys.add(key);
            }

            if (!newKeys.isEmpty()) {
                plugin
                        .getLogger()
                        .info("Added new language keys: " + String.join(", ", newKeys));
                try {
                    localeConfig.save(file);
                } catch (IOException e) {
                    plugin
                            .getLogger()
                            .log(Level.WARNING, "Unable to save resource " + locale + ".yml", e);
                }
            }
        }

        if (!locale.equals(defaultLocale)) {
            localeConfigDefaults = locales.get(defaultLocale);

            // Check for missing keys
//            List<String> newKeys = new ArrayList<>();
//            for (String key : localeConfigDefaults.getKeys(true)) {
//                if (localeConfigDefaults.isConfigurationSection(key)) {
//                    continue;
//                }
//
//                if (localeConfig.isSet(key)) {
//                    continue;
//                }
//
//                newKeys.add(key);
//            }
//
//            if (!newKeys.isEmpty()) {
//                plugin
//                        .getLogger()
//                        .info(
//                                "[Metropolis] Missing translations from "
//                                        + locale
//                                        + ".yml: "
//                                        + String.join(", ", newKeys));
//            }

            // Fall through to default locale
            localeConfig.setDefaults(localeConfigDefaults);
        }

        locales.put(locale, localeConfig);
        return localeConfig;
    }

    @Nullable
    public String getValue(@NotNull String key, @Nullable String locale) {
        String value =
                getOrLoadLocale(locale == null ? defaultLocale : locale.toLowerCase()).getString(key);
        if (value == null || value.isEmpty()) {
            return null;
        }

        return value;
    }

    @Nullable
    public String getValue(
            @NotNull String key, @Nullable String locale, @NotNull String... replacements) {
        if (replacements.length % 2 != 0) {
            plugin
                    .getLogger()
                    .log(Level.WARNING, "Replacement data is uneven", new Exception());
        }

        String value = getValue(key, locale);

        if (value == null) {
            return null;
        }

        for (int i = 0; i < replacements.length; i += 2) {
            value = value.replace(replacements[i], replacements[i + 1]);
        }

        return value;
    }

}