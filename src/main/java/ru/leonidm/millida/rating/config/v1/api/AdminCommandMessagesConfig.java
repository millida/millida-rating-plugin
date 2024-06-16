package ru.leonidm.millida.rating.config.v1.api;

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

}
