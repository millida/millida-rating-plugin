package ru.leonidm.millida.rating.config.api;

import org.jetbrains.annotations.NotNull;

public interface MessagesConfig {

    @NotNull
    String getPrefix();

    @NotNull
    CommandsMessagesConfig getCommandsMessagesConfig();
}
