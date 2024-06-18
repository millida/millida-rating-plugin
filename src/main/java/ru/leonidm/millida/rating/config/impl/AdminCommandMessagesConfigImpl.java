package ru.leonidm.millida.rating.config.impl;

import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.config.ConfigLoadException;
import ru.leonidm.millida.rating.config.ConfigUtils;
import ru.leonidm.millida.rating.config.api.AdminCommandMessagesConfig;
import ru.leonidm.millida.rating.config.api.HologramCommandMessagesConfig;

@Data
public class AdminCommandMessagesConfigImpl implements AdminCommandMessagesConfig {

    private final String usage;
    private final String unknownPlayer;
    private final String disablingModule;
    private final String disablingPlugin;
    private final HologramCommandMessagesConfig hologramCommandMessagesConfig;

    public AdminCommandMessagesConfigImpl(@NotNull ConfigurationSection section) throws ConfigLoadException {
        usage = ConfigUtils.getString(section, "usage");
        unknownPlayer = ConfigUtils.getString(section, "unknown_player");
        disablingModule = ConfigUtils.getString(section, "disabling_module");
        disablingPlugin = ConfigUtils.getString(section, "disabling_plugin");

        hologramCommandMessagesConfig = new HologramCommandMessagesConfigImpl(ConfigUtils.getSection(section, "hologram"));
    }
}
