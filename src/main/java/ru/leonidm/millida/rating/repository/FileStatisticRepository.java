package ru.leonidm.millida.rating.repository;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.api.repository.StatisticRepository;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class FileStatisticRepository implements StatisticRepository {

    protected final Logger logger = Logger.getLogger("MillidaRating");

    private final Properties properties = new Properties();
    private final Plugin plugin;
    private final Path path;

    public FileStatisticRepository(@NotNull Plugin plugin) {
        this.plugin = plugin;

        path = plugin.getDataFolder().toPath().resolve("statistic.properties");
    }

    @Override
    public void initialize() {
        try {
            if (Files.exists(path)) {
                properties.load(Files.newBufferedReader(path));
            } else {
                properties.put("last-vote", "0");
                save(() -> "tried to save last-vote=0");
            }
        } catch (IOException e) {
            logger.severe("Cannot load 'statistic.properties' (did you change it?)");
            e.printStackTrace();

            logger.severe(">>> Disabling the plugin");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    @Override
    public long getLastVote() {
        String lastVote = properties.getProperty("last-vote");
        try {
            return Long.parseLong(lastVote);
        } catch (NumberFormatException e) {
            logger.severe("Statistic 'last-vote' is broken (did you change statistic.properties?)");
            return Integer.MAX_VALUE;
        }
    }

    @Override
    public void setLastVote(long lastVote) {
        properties.put("last-vote", String.valueOf(lastVote));

        save(() -> "tried to save last-vote=" + lastVote);
    }

    private void save(@NotNull Supplier<String> exceptionMessage) {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            properties.store(writer, "Do not change this file!");
        } catch (IOException e) {
            logger.severe("Cannot save 'statistic.properties': " + exceptionMessage.get());
            e.printStackTrace();

            logger.severe(">>> Disabling the plugin");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    @Override
    public void close() {

    }
}
