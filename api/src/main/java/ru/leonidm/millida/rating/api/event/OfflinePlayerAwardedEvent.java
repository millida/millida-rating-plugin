package ru.leonidm.millida.rating.api.event;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class OfflinePlayerAwardedEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final OfflinePlayer offlinePlayer;

    public OfflinePlayerAwardedEvent(@NotNull OfflinePlayer offlinePlayer) {
        this.offlinePlayer = offlinePlayer;
    }

    @NotNull
    public OfflinePlayer getPlayer() {
        return offlinePlayer;
    }

    @Override
    @NotNull
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
