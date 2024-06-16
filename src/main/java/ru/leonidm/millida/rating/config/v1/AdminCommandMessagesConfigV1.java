package ru.leonidm.millida.rating.config.v1;

import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.config.ConfigLoadException;
import ru.leonidm.millida.rating.config.ConfigUtils;
import ru.leonidm.millida.rating.config.v1.api.AdminCommandMessagesConfig;

@Data
public class AdminCommandMessagesConfigV1 implements AdminCommandMessagesConfig {

    private final String usage;
    private final String unknownPlayer;
    private final String disablingModule;
    private final String disablingPlugin;

    public AdminCommandMessagesConfigV1(@NotNull ConfigurationSection section) throws ConfigLoadException {
        usage = ConfigUtils.getString(section, "usage");
        unknownPlayer = ConfigUtils.getString(section, "unknown_player");
        disablingModule = ConfigUtils.getString(section, "disabling_module");
        disablingPlugin = ConfigUtils.getString(section, "disabling_plugin");
    }
}
