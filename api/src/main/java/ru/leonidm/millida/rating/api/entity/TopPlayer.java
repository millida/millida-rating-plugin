package ru.leonidm.millida.rating.api.entity;

import lombok.Data;

@Data
public class TopPlayer {

    private final long id;
    private final String authorName;
    private final String nickname;
    private final long voteCount;

}
