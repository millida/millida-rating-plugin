package ru.leonidm.millida.rating.config.impl;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import ru.leonidm.millida.rating.config.ConfigLoadException;
import ru.leonidm.millida.rating.config.ConfigUtils;
import ru.leonidm.millida.rating.config.api.Reward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class RewardImpl implements Reward {

    private final String command;
    private final boolean online;

    public RewardImpl(@NotNull ConfigurationSection section) throws ConfigLoadException {
        command = ConfigUtils.getString(section, "command");
        online = ConfigUtils.getBoolean(section, "online");
    }

    @NotNull
    @Unmodifiable
    public static List<Reward> parseList(@NotNull ConfigurationSection section) throws ConfigLoadException {
        List<Reward> rewardList = new ArrayList<>();

        for (String key : section.getKeys(false)) {
            rewardList.add(new RewardImpl(ConfigUtils.getSection(section, key)));
        }

        return Collections.unmodifiableList(rewardList);
    }

    @NotNull
    public String getName(@NotNull OfflinePlayer offlinePlayer) {
        return command.replace("{player}", offlinePlayer.getName());
    }

    public void apply(@NotNull OfflinePlayer offlinePlayer, boolean online) {
        String finalCommand = command.replace("{player}", offlinePlayer.getName())
                .replace("{uuid}", offlinePlayer.getUniqueId().toString());

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
    }
}