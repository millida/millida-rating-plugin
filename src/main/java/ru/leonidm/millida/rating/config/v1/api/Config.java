package ru.leonidm.millida.rating.config.v1.api;

import org.jetbrains.annotations.NotNull;

public interface Config {

    int getServerId();

    int getRequestPeriod();

    int getTopRequestPeriod();

    @NotNull
    Rewards getRewards();

    @NotNull
    ConnectionFactory getDatabase();

}
