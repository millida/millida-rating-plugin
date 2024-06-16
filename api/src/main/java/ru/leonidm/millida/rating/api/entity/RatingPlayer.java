package ru.leonidm.millida.rating.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class RatingPlayer {

    private final UUID uuid;
    private int streak;
    private long lastVoteTimestamp;

}
