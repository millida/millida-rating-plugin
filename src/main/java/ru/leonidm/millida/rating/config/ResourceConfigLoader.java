package ru.leonidm.millida.rating.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.config.api.Config;
import ru.leonidm.millida.rating.config.api.HologramsConfig;
import ru.leonidm.millida.rating.config.api.MessagesConfig;
import ru.leonidm.millida.rating.external.utils.LazySupplier;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ResourceConfigLoader {

    private ResourceConfigLoader() {

    }

    @NotNull
    public static Config load(@NotNull Plugin plugin, @NotNull String name) throws ConfigLoadException {
        FileConfiguration config = getConfig(plugin, name);

        return ConfigLoader.load(plugin, config);
    }

    @NotNull
    public static HologramsConfig loadHolograms(@NotNull Plugin plugin, @NotNull String name) throws ConfigLoadException {
        FileConfiguration config = getConfig(plugin, name);

        return ConfigLoader.loadHolograms(plugin, config);
    }

    @NotNull
    public static MessagesConfig loadMessages(@NotNull Plugin plugin, @NotNull String name) throws ConfigLoadException {
        FileConfiguration config = getConfig(plugin, name);
        Supplier<FileConfiguration> defaultConfig = LazySupplier.of(() -> getDefaultConfig(plugin, name));

        Consumer<String> copyKey = key -> {
            config.set(key, defaultConfig.get().get(key));
        };

        int version = ConfigUtils.getInt(config, "config_version");

        if (version == 1) {
            copyKey.accept("commands.admin.hologram.not_found");
            version = 2;
        }

        if (config.getInt("config_version") != version) {
            config.set("config_version", 2);
            save(plugin, config, "messages.yml");
        }

        return ConfigLoader.loadMessages(plugin, config);
    }

    @NotNull
    private static FileConfiguration getConfig(@NotNull Plugin plugin, @NotNull String name) {
        File configFile = new File(plugin.getDataFolder(), name);
        if (!configFile.exists()) {
            plugin.saveResource(name, false);
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }

    @NotNull
    private static FileConfiguration getDefaultConfig(@NotNull Plugin plugin, @NotNull String name) {
        return YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(name), StandardCharsets.UTF_8));
    }

    private static void save(@NotNull Plugin plugin, @NotNull FileConfiguration config, @NotNull String name) {
        try {
            config.save(new File(plugin.getDataFolder(), name));
        } catch (IOException e) {
            plugin.getLogger().severe("Cannot save config " + name);
            e.printStackTrace();
        }
    }
}
