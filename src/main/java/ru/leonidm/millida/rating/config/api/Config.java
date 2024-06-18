package ru.leonidm.millida.rating.config.api;

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
