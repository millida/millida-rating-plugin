package ru.leonidm.millida.rating.config.v1;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.config.ConfigLoadException;
import ru.leonidm.millida.rating.config.ConfigUtils;
import ru.leonidm.millida.rating.config.v1.api.AdminCommandMessagesConfig;
import ru.leonidm.millida.rating.config.v1.api.CommandsMessagesConfig;

@Getter
public class CommandsMessagesConfigV1 implements CommandsMessagesConfig {

    private final AdminCommandMessagesConfig adminCommandMessagesConfig;
    private final String ok;

    public CommandsMessagesConfigV1(@NotNull ConfigurationSection section) throws ConfigLoadException {
        this.adminCommandMessagesConfig = new AdminCommandMessagesConfigV1(ConfigUtils.getSection(section, "admin"));
        this.ok = ConfigUtils.getString(section, "ok");
    }
}
