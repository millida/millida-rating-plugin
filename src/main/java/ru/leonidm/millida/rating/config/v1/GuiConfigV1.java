package ru.leonidm.millida.rating.config.v1;

import lombok.Data;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.millida.rating.config.ConfigLoadException;
import ru.leonidm.millida.rating.config.ConfigUtils;
import ru.leonidm.millida.rating.config.v1.api.GuiConfig;
import ru.leonidm.millida.rating.external.utils.IntRangeParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
public class GuiConfigV1 implements GuiConfig {

    private final boolean enabled;
    private final String command;
    private final String title;
    private final ItemStack defaultRewardIcon;
    private final Map<Integer, ItemStack> overrodeRewardIcons;
    private final Material defaultCompletedIcon;
    private final Map<Integer, Material> overrodeCompletedIcons;
    private final int messageSlot;
    private final String message;
    private final ItemStack defaultCustomIcon;
    private final Map<Integer, ItemStack> overrodeCustomIcons;

    public GuiConfigV1(@NotNull ConfigurationSection section) throws ConfigLoadException {
        enabled = ConfigUtils.getBoolean(section, "enabled");

        command = ConfigUtils.getString(section, "command");

        title = ConfigUtils.getString(section, "title");

        ConfigurationSection rewards = ConfigUtils.getSection(section, "rewards");

        ConfigurationSection defaultReward = ConfigUtils.getSection(rewards, "default");
        defaultRewardIcon = ConfigUtils.getItemStack(defaultReward);
        overrodeRewardIcons = loadIcons(rewards, 1, 28);

        ConfigurationSection completed = ConfigUtils.getSection(section, "icons.completed");
        defaultCompletedIcon = ConfigUtils.getMaterial(completed, "default");
        overrodeCompletedIcons = loadMaterials(completed, 1, 28);

        messageSlot = ConfigUtils.getPositiveInt(section, "slots.message");
        message = ConfigUtils.getString(section, "message");

        ConfigurationSection customIcons = ConfigUtils.getSection(section, "icons.custom");

        if (customIcons.contains("default")) {
            defaultCustomIcon = ConfigUtils.getItemStack(ConfigUtils.getSection(customIcons, "default"));
        } else {
            defaultCustomIcon = null;
        }

        overrodeCustomIcons = loadIcons(customIcons, 0, 53);
    }

    @NotNull
    private Map<Integer, ItemStack> loadIcons(@NotNull ConfigurationSection section, int min, int max) throws ConfigLoadException {
        Map<Integer, ItemStack> result = new HashMap<>();

        Set<String> keys = section.getKeys(false);
        keys.remove("default");

        for (String key : keys) {
            int[] days;
            try {
                days = IntRangeParser.parseIntRange(key).toArray();
            } catch (IllegalArgumentException e) {
                throw ConfigUtils.loadException(section, key, "is bad configured range");
            }

            for (int day : days) {
                try {
                    if (day < min || day > max) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    throw ConfigUtils.loadException(section, key, "must be " + (min > 0 ? "positive " : "") +
                            "integers less than or equal to 28");
                }

                result.put(day, ConfigUtils.getItemStack(ConfigUtils.getSection(section, key)));
            }
        }

        return result;
    }

    @NotNull
    private Map<Integer, Material> loadMaterials(@NotNull ConfigurationSection section, int min, int max) throws ConfigLoadException {
        Map<Integer, Material> result = new HashMap<>();

        Set<String> keys = section.getKeys(false);
        keys.remove("default");

        for (String key : keys) {
            int[] days;
            try {
                days = IntRangeParser.parseIntRange(key).toArray();
            } catch (IllegalArgumentException e) {
                throw ConfigUtils.loadException(section, key, "is bad configured range");
            }

            for (int day : days) {
                try {
                    if (day < min || day > max) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException e) {
                    throw ConfigUtils.loadException(section, key, "must be " + (min > 0 ? "positive " : "") +
                            "integers less than or equal to 28");
                }

                result.put(day, ConfigUtils.getMaterial(section, key));
            }
        }

        return result;
    }

    @Override
    @NotNull
    public ItemStack getRewardIcon(int day) {
        return overrodeRewardIcons.getOrDefault(day, defaultRewardIcon).clone();
    }

    @NotNull
    public Material getCompletedIcon(int day) {
        return overrodeCompletedIcons.getOrDefault(day, defaultCompletedIcon);
    }

    @Override
    @Nullable
    public ItemStack getCustomIcon(int slot) {
        ItemStack itemStack = overrodeCustomIcons.getOrDefault(slot, defaultCustomIcon);
        return itemStack != null ? itemStack.clone() : null;
    }
}
