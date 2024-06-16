package ru.leonidm.millida.rating.api.event;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.api.entity.Vote;

public class RatingVoteEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final Vote vote;
    private final OfflinePlayer offlinePlayer;

    public RatingVoteEvent(@NotNull Vote vote, @NotNull OfflinePlayer offlinePlayer) {
        super(true);
        this.vote = vote;
        this.offlinePlayer = offlinePlayer;
    }

    @NotNull
    public Vote getVote() {
        return vote;
    }

    @NotNull
    public OfflinePlayer getOfflinePlayer() {
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
