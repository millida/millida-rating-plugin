package ru.leonidm.millida.rating.config.impl;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.config.ConfigLoadException;
import ru.leonidm.millida.rating.config.ConfigUtils;
import ru.leonidm.millida.rating.config.api.Config;
import ru.leonidm.millida.rating.config.api.ConnectionFactory;
import ru.leonidm.millida.rating.config.api.Rewards;

@Getter
public class ConfigImpl implements Config {

    private final int serverId;
    private final int requestPeriod;
    private final int topRequestPeriod;
    private final Rewards rewards;
    private final ConnectionFactory database;

    public ConfigImpl(@NotNull Plugin plugin, @NotNull ConfigurationSection section) throws ConfigLoadException {
        serverId = ConfigUtils.getPositiveInt(section, "server_id");
        requestPeriod = Math.max(15, ConfigUtils.getPositiveInt(section, "request_period"));
        topRequestPeriod = Math.max(15, ConfigUtils.getPositiveInt(section, "top_request_period"));
        rewards = new RewardsImpl(ConfigUtils.getSection(section, "rewards"));
        database = ConnectionFactory.from(plugin, ConfigUtils.getSection(section, "database"));
    }
}
