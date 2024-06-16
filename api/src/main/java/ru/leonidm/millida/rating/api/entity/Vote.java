package ru.leonidm.millida.rating.api.entity;

import lombok.Data;

@Data
public class Vote {

    private final long id;
    private final String authorName;
    private final String nickname;
    private final long voteId;
    private final long voteData;

    public long getVoteTimestamp() {
        return voteData;
    }

}
