package ru.leonidm.millida.rating.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeferredReward {

    private final long id;
    private final int day;

    public DeferredReward(int day) {
        this(-1, day);
    }

}
