package ru.leonidm.millida.rating.config.api;

import org.jetbrains.annotations.NotNull;

public interface CommandsMessagesConfig {

    @NotNull
    AdminCommandMessagesConfig getAdminCommandMessagesConfig();

    @NotNull
    String getOk();
}
