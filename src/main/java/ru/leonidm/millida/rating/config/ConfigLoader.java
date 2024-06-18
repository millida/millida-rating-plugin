package ru.leonidm.millida.rating.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.config.api.Config;
import ru.leonidm.millida.rating.config.api.HologramsConfig;
import ru.leonidm.millida.rating.config.api.MessagesConfig;
import ru.leonidm.millida.rating.config.impl.ConfigImpl;
import ru.leonidm.millida.rating.config.impl.HologramsConfigImpl;
import ru.leonidm.millida.rating.config.impl.MessagesConfigImpl;

public final class ConfigLoader {

    private ConfigLoader() {

    }

    @NotNull
    public static Config load(@NotNull Plugin plugin, @NotNull ConfigurationSection section) throws ConfigLoadException {
        int version = ConfigUtils.getInt(section, "config_version");

        if (version == 1) {
            return new ConfigImpl(plugin, section);
        } else {
            throw new ConfigLoadException("Unsupported " + version + " version of config.yml");
        }
    }

    @NotNull
    public static HologramsConfig loadHolograms(@NotNull Plugin plugin, @NotNull ConfigurationSection section) throws ConfigLoadException {
        int version = ConfigUtils.getInt(section, "config_version");

        if (version == 1) {
            return new HologramsConfigImpl(section);
        } else {
            throw new ConfigLoadException("Unsupported " + version + " version of holograms.yml");
        }
    }

    @NotNull
    public static MessagesConfig loadMessages(@NotNull Plugin plugin, @NotNull ConfigurationSection section) throws ConfigLoadException {
        int version = ConfigUtils.getInt(section, "config_version");

        if (version == 2) {
            return new MessagesConfigImpl(section);
        } else {
            throw new ConfigLoadException("Unsupported " + version + " version of messages.yml");
        }
    }
}
