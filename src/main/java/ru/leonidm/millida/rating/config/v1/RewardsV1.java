package ru.leonidm.millida.rating.config.v1;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import ru.leonidm.millida.rating.config.ConfigLoadException;
import ru.leonidm.millida.rating.config.ConfigUtils;
import ru.leonidm.millida.rating.config.v1.api.GuiConfig;
import ru.leonidm.millida.rating.config.v1.api.Reward;
import ru.leonidm.millida.rating.config.v1.api.Rewards;
import ru.leonidm.millida.rating.external.utils.IntRangeParser;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RewardsV1 implements Rewards {

    @Getter
    private final boolean enabled;
    private final List<Reward> defaultReward;
    private final Map<Integer, List<Reward>> overrodeRewards;
    @Getter
    private final GuiConfig guiConfig;

    public RewardsV1(@NotNull ConfigurationSection section) throws ConfigLoadException {
        this.enabled = ConfigUtils.getBoolean(section, "enabled");

        defaultReward = RewardV1.parseList(ConfigUtils.getSection(section, "default"));

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

                overrodeRewards.put(day, RewardV1.parseList(ConfigUtils.getSection(override, key)));
            }
        }

        this.overrodeRewards = Collections.unmodifiableMap(overrodeRewards);

        guiConfig = new GuiConfigV1(ConfigUtils.getSection(section, "gui"));
    }

    @Override
    @NotNull
    @Unmodifiable
    public List<Reward> getReward(int day) {
        return overrodeRewards.getOrDefault(day, defaultReward);
    }
}
