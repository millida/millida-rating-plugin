package ru.leonidm.millida.rating.config.api;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public interface Reward {

    @NotNull
    String getName(@NotNull OfflinePlayer offlinePlayer);

    boolean isOnline();

    void apply(@NotNull OfflinePlayer offlinePlayer, boolean online);

}
