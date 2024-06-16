package ru.leonidm.millida.rating.config.v1.api;

import org.jetbrains.annotations.NotNull;

public interface CommandsMessagesConfig {

    @NotNull
    AdminCommandMessagesConfig getAdminCommandMessagesConfig();

    @NotNull
    String getOk();
}
