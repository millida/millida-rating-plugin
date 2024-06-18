package ru.leonidm.millida.rating.config.impl;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import ru.leonidm.millida.rating.config.ConfigLoadException;
import ru.leonidm.millida.rating.config.ConfigUtils;
import ru.leonidm.millida.rating.config.api.GuiConfig;
import ru.leonidm.millida.rating.config.api.Reward;
import ru.leonidm.millida.rating.config.api.Rewards;
import ru.leonidm.millida.rating.external.utils.IntRangeParser;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardsImpl implements Rewards {

    @Getter
    private final boolean enabled;
    private final List<Reward> defaultReward;
    private final Map<Integer, List<Reward>> overrodeRewards;
    @Getter
    private final GuiConfig guiConfig;

    public RewardsImpl(@NotNull ConfigurationSection section) throws ConfigLoadException {
        this.enabled = ConfigUtils.getBoolean(section, "enabled");

        defaultReward = RewardImpl.parseList(ConfigUtils.getSection(section, "default"));

        Map<Integer, List<Reward>> overrodeRewards = new HashMap<>();

        ConfigurationSection override = ConfigUtils.getSection(section, "override");
        for (String key : override.getKeys(false)) {
            int[] days;
            try {
                days = IntRangeParser.parseIntRange(key).toArray();
            } catch (IllegalArgumentException e) {
                throw ConfigUtils.loadException(override, key, "is bad configured range");
            }

            for (int day : days) {
                try {
                    if (day <= 0 || day > 28) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    throw ConfigUtils.loadException(override, key, "must be positive integers less than or equal to 28");
                }

                overrodeRewards.put(day, RewardImpl.parseList(ConfigUtils.getSection(override, key)));
            }
        }

        this.overrodeRewards = Collections.unmodifiableMap(overrodeRewards);

        guiConfig = new GuiConfigImpl(ConfigUtils.getSection(section, "gui"));
    }

    @NotNull
    @Unmodifiable
    public List<Reward> getReward(int day) {
        return overrodeRewards.getOrDefault(day, defaultReward);
    }
}
