package ru.leonidm.millida.rating.config.v1;

import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.config.ConfigLoadException;
import ru.leonidm.millida.rating.config.ConfigUtils;
import ru.leonidm.millida.rating.config.v1.api.CommandsMessagesConfig;
import ru.leonidm.millida.rating.config.v1.api.MessagesConfig;

@Data
public class MessagesConfigV1 implements MessagesConfig {

    private final String prefix;
    private final CommandsMessagesConfig commandsMessagesConfig;

    public MessagesConfigV1(@NotNull ConfigurationSection section) throws ConfigLoadException {
        prefix = ConfigUtils.getString(section, "prefix");
        commandsMessagesConfig = new CommandsMessagesConfigV1(ConfigUtils.getSection(section, "commands"));
    }
}
