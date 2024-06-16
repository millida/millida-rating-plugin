package ru.leonidm.millida.rating.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.config.v1.MessagesConfigV1;
import ru.leonidm.millida.rating.config.v1.api.Config;
import ru.leonidm.millida.rating.config.v1.api.HologramsConfig;
import ru.leonidm.millida.rating.config.v1.api.MessagesConfig;
import ru.leonidm.millida.rating.config.v1.ConfigV1;
import ru.leonidm.millida.rating.config.v1.HologramsConfigV1;

public final class ConfigLoader {

    private ConfigLoader() {

    }

    @NotNull
    public static Config load(@NotNull Plugin plugin, @NotNull ConfigurationSection section) throws ConfigLoadException {
        int version = section.getInt("config_version");

        if (version == 1) {
            return new ConfigV1(plugin, section);
        } else {
            throw new ConfigLoadException("Unknown " + version + " version of config.yml");
        }
    }

    @NotNull
    public static HologramsConfig loadHolograms(@NotNull Plugin plugin, @NotNull ConfigurationSection section) throws ConfigLoadException {
        int version = section.getInt("config_version");

        if (version == 1) {
            return new HologramsConfigV1(section);
        } else {
            throw new ConfigLoadException("Unknown " + version + " version of holograms.yml");
        }
    }

    @NotNull
    public static MessagesConfig loadMessages(@NotNull Plugin plugin, @NotNull ConfigurationSection section) throws ConfigLoadException {
        int version = section.getInt("config_version");

        if (version == 1) {
            return new MessagesConfigV1(section);
        } else {
            throw new ConfigLoadException("Unknown " + version + " version of messages.yml");
        }
    }
}
