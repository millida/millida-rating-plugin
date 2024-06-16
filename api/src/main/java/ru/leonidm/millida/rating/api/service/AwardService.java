package ru.leonidm.millida.rating.api.service;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface AwardService {

    void awardAll(@NotNull Player player, int day);

    void award(@NotNull OfflinePlayer player, int day, boolean online);

}
