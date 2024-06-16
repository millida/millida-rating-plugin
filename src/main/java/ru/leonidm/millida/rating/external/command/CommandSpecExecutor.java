package ru.leonidm.millida.rating.external.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

@FunctionalInterface
public interface CommandSpecExecutor extends BiConsumer<CommandSender, String[]> {

    void execute(@NotNull CommandSender sender, @NotNull String @NotNull [] args);

    @Override
    default void accept(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        execute(sender, args);
    }
}
