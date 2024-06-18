package ru.leonidm.millida.rating.config.api;

import org.jetbrains.annotations.NotNull;

public interface AdminCommandMessagesConfig {

    @NotNull
    String getUsage();

    @NotNull
    String getUnknownPlayer();

    @NotNull
    String getDisablingModule();

    @NotNull
    String getDisablingPlugin();

    @NotNull
    HologramCommandMessagesConfig getHologramCommandMessagesConfig();

}
