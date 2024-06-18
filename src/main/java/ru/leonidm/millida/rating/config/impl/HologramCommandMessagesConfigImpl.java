package ru.leonidm.millida.rating.config.impl;

import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.config.ConfigLoadException;
import ru.leonidm.millida.rating.config.ConfigUtils;
import ru.leonidm.millida.rating.config.api.HologramCommandMessagesConfig;

@Data
public class HologramCommandMessagesConfigImpl implements HologramCommandMessagesConfig {

    private final String notFound;

    public HologramCommandMessagesConfigImpl(@NotNull ConfigurationSection section) throws ConfigLoadException {
        notFound = ConfigUtils.getString(section, "not_found");
    }
}
