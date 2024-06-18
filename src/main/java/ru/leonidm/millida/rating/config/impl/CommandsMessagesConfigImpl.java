package ru.leonidm.millida.rating.config.impl;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.config.ConfigLoadException;
import ru.leonidm.millida.rating.config.ConfigUtils;
import ru.leonidm.millida.rating.config.api.AdminCommandMessagesConfig;
import ru.leonidm.millida.rating.config.api.CommandsMessagesConfig;

@Getter
public class CommandsMessagesConfigImpl implements CommandsMessagesConfig {

    private final AdminCommandMessagesConfig adminCommandMessagesConfig;
    private final String ok;

    public CommandsMessagesConfigImpl(@NotNull ConfigurationSection section) throws ConfigLoadException {
        this.adminCommandMessagesConfig = new AdminCommandMessagesConfigImpl(ConfigUtils.getSection(section, "admin"));
        this.ok = ConfigUtils.getString(section, "ok");
    }
}
