package ru.leonidm.millida.rating.config.impl;

import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.config.ConfigLoadException;
import ru.leonidm.millida.rating.config.ConfigUtils;
import ru.leonidm.millida.rating.config.api.CommandsMessagesConfig;
import ru.leonidm.millida.rating.config.api.MessagesConfig;

@Data
public class MessagesConfigImpl implements MessagesConfig {

    private final String prefix;
    private final CommandsMessagesConfig commandsMessagesConfig;

    public MessagesConfigImpl(@NotNull ConfigurationSection section) throws ConfigLoadException {
        prefix = ConfigUtils.getString(section, "prefix");
        commandsMessagesConfig = new CommandsMessagesConfigImpl(ConfigUtils.getSection(section, "commands"));
    }
}
