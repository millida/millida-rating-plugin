package ru.leonidm.millida.rating.external.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiFunction;

@FunctionalInterface
public interface CommandSpecTabCompleter extends BiFunction<CommandSender, String[], List<String>> {

    @Nullable
    List<String> complete(@NotNull CommandSender sender, @NotNull String @NotNull [] args);

    @Override
    @Nullable
    default List<String> apply(@NotNull CommandSender sender, @NotNull String @NotNull [] args) {
        return complete(sender, args);
    }
}
