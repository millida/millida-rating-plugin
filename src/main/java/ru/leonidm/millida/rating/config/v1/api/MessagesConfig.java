package ru.leonidm.millida.rating.config.v1.api;

import org.jetbrains.annotations.NotNull;

public interface MessagesConfig {

    @NotNull
    String getPrefix();

    @NotNull
    CommandsMessagesConfig getCommandsMessagesConfig();
}
