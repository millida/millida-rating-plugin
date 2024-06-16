package ru.leonidm.millida.rating.external.command;

import static com.google.common.base.Preconditions.checkNotNull;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Облегчает создание команд с вложенной структурой и табкамплитером
 * @author ArtBorax
 */
public final class CommandSpec implements TabExecutor {

    private final Optional<String> description;
    private final Optional<String> msgNoPermission;
    private final Optional<String> usage;
    private final Optional<String> permission;
    private final Optional<BiConsumer<CommandSender, String[]>> executor;
    private final Optional<BiFunction<CommandSender, String[], List<String>>> completer;
    private final Map<String, CommandSpec> child;

    private CommandSpec(@Nullable BiConsumer<CommandSender, String[]> executor,
                        @Nullable BiFunction<CommandSender, String[], List<String>> completer,
                        @Nullable String description,
                        @Nullable String msgNoPermission,
                        @Nullable String usage,
                        @Nullable String permission,
                        @Nullable Map<String, CommandSpec> child) {
        this.executor = Optional.ofNullable(executor);
        this.completer = Optional.ofNullable(completer);
        this.usage = Optional.ofNullable(usage);
        this.permission = Optional.ofNullable(permission);
        this.description = Optional.ofNullable(description);
        this.msgNoPermission = Optional.ofNullable(msgNoPermission);
        this.child = child != null ? child : new HashMap<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Отправить пользователю все дескрипшины команд иерархический если они установлены и если у пользователя есть права
     * Очень полезна для команды /help
     *
     * @param sender кому отправлять результат
     */
    public void printDescription(@NotNull CommandSender sender) {
        if (!permission.map(s -> !sender.hasPermission(s)).orElse(false)) {
            description.ifPresent(sender::sendMessage);
            child.values().forEach(commandSpec -> commandSpec.printDescription(sender));
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!permission.map(s -> !sender.hasPermission(s)).orElse(false)) {
            if (args.length == 0) {
                executor.ifPresent(consumer -> consumer.accept(sender, args));
                if (!executor.isPresent()) {
                    usage.ifPresent(sender::sendMessage);
                }
            } else {
                String[] cropArgs = new String[args.length - 1];
                if (cropArgs.length > 0) {
                    System.arraycopy(args, 1, cropArgs, 0, cropArgs.length);
                }
                CommandSpec commandSpec = child.get(args[0].toLowerCase(Locale.ROOT));
                if (commandSpec != null) {
                    commandSpec.onCommand(sender, command, label, cropArgs);
                } else {
                    executor.ifPresent(consumer -> consumer.accept(sender, args));
                    if (!executor.isPresent()) {
                        usage.ifPresent(sender::sendMessage);
                    }
                }
            }
        } else {
            msgNoPermission.ifPresent(sender::sendMessage);
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (permission.map(s -> !sender.hasPermission(s)).orElse(false)) {
            return Collections.emptyList();
        }
        String cmd = args[0].toLowerCase(Locale.ROOT);
        CommandSpec commandSpec = child.get(cmd);
        if (commandSpec != null) {
            if (args.length > 1) {
                String[] cropArgs = new String[args.length - 1];
                System.arraycopy(args, 1, cropArgs, 0, cropArgs.length);
                return commandSpec.onTabComplete(sender, command, label, cropArgs);
            }
            return Collections.emptyList();
        } else if (completer.isPresent()) {
            return completer.get().apply(sender, args);
        }
        return child.entrySet().stream()
                .filter(e -> e.getKey().startsWith(cmd))
                .filter(e -> e.getValue().permission.map(sender::hasPermission).orElse(true))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Билдер объекта {@link CommandSpec}
     */
    public static final class Builder {
        @Nullable
        private String description;
        @Nullable
        private String msgNoPermission;
        @Nullable
        private String usage;
        @Nullable
        private String permission;
        @Nullable
        private BiConsumer<CommandSender, String[]> executor;
        @Nullable
        private BiFunction<CommandSender, String[], List<String>> completer;
        @Nullable
        private Map<String, CommandSpec> childCommandMap;

        Builder() {
        }

        /**
         * Устанавливает дескрипшен, для рапечатывания через метод {@link CommandSpec#printDescription}
         *
         * @param description
         * @return {@link Builder}
         */
        public Builder description(@Nullable String description) {
            this.description = description;
            return this;
        }

        /**
         * Устанавливает сообщение, которое будет отправлено, если пользователю не хватает прав.
         *
         * @param msgNoPermission сообщение
         * @return {@link Builder}
         */
        public Builder msgNoPermission(@Nullable String msgNoPermission) {
            this.msgNoPermission = msgNoPermission;
            return this;
        }

        /**
         * Устанавливает сообщение, которое будет отправлено, если пользователь ввел не верную команду
         *
         * @param usage сообщение
         * @return {@link Builder}
         */
        public Builder usage(@Nullable String usage) {
            this.usage = usage;
            return this;
        }

        /**
         * Устанавливает права необходимые пользователю для вызова данной команды
         *
         * @param permission права, например "mute.*"
         * @return {@link Builder}
         */
        public Builder permission(@Nullable String permission) {
            this.permission = permission;
            return this;
        }

        /**
         * Устанавливает реализацию команды, также если передаваемый класс реализует BiFunction&lt;CommandSender, String[], List&lt;String&gt;&gt;
         * то также устанавливает этот же инстанс на табкомплитер и вызов {@link Builder#completer} уже не обязателен.
         *
         * @param executor реализация выполнения команды+табкомплитер
         * @return {@link Builder}
         */
        @SuppressWarnings("java:S108")//Подавление пустого Exception
        public Builder executor(@Nullable BiConsumer<CommandSender, String[]> executor) {
            this.executor = executor;
            if (executor instanceof BiFunction) {
                try {
                    this.completer = (BiFunction<CommandSender, String[], List<String>>) executor;
                } catch (Exception ignore) {
                }
            }
            return this;
        }

        /**
         * Устанавливает реализацию на табкомплитер
         *
         * @param completer табкомплитер
         * @return {@link Builder}
         */
        public Builder completer(BiFunction<CommandSender, String[], List<String>> completer) {
            this.completer = completer;
            return this;
        }

        /**
         * Устанавливает дочернии команды (подкоманды) команд
         *
         * @param child   Реализация дочерней команды
         * @param cmd     Дочерняя команда
         * @param aliases Алиасы
         * @return {@link Builder}
         */
        public Builder child(@NotNull CommandSpec child, @NotNull String cmd, @NotNull String... aliases) {
            if (this.childCommandMap == null) {
                this.childCommandMap = new HashMap<>();
            }
            this.childCommandMap.put(cmd.toLowerCase(Locale.ROOT), child);
            for (String alias : aliases) {
                this.childCommandMap.put(alias.toLowerCase(Locale.ROOT), child);
            }
            return this;
        }

        /**
         * Собирает объект обработки команды
         *
         * @return {@link CommandSpec}
         */
        public @NotNull CommandSpec build() {
            if (this.childCommandMap == null || this.childCommandMap.isEmpty()) {
                checkNotNull(this.executor, "An executor is required");
            }
            return new CommandSpec(this.executor, this.completer, this.description, this.msgNoPermission, this.usage, this.permission, this.childCommandMap);
        }
    }
}
