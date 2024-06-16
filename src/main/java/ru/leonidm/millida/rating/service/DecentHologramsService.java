package ru.leonidm.millida.rating.service;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.actions.Action;
import eu.decentsoftware.holograms.api.actions.ActionType;
import eu.decentsoftware.holograms.api.actions.ClickType;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.MillidaRatingPlugin;
import ru.leonidm.millida.rating.api.entity.TopPlayer;
import ru.leonidm.millida.rating.api.service.HologramsService;
import ru.leonidm.millida.rating.api.service.RatingRequester;
import ru.leonidm.millida.rating.config.ConfigLoadException;
import ru.leonidm.millida.rating.config.ConfigUtils;
import ru.leonidm.millida.rating.config.v1.api.HologramLines;
import ru.leonidm.millida.rating.config.v1.api.HologramsConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class DecentHologramsService implements HologramsService {

    private final Set<Hologram> holograms = new LinkedHashSet<>();
    private final MillidaRatingPlugin plugin;
    private final HologramsConfig hologramsConfig;
    private final RatingRequester ratingRequester;
    private int index;

    public DecentHologramsService(@NotNull MillidaRatingPlugin plugin, @NotNull HologramsConfig hologramsConfig,
                                  @NotNull RatingRequester ratingRequester) {
        this.plugin = plugin;
        this.hologramsConfig = hologramsConfig;
        this.ratingRequester = ratingRequester;
    }

    @Override
    public void initialize() {
        for (Location location : hologramsConfig.getLocations()) {
            createHologram(location, false);
        }
    }

    private void addPage(int index, @NotNull Hologram hologram, @NotNull HologramLines hologramLines,
                         @NotNull Supplier<List<TopPlayer>> supplier) {
        HologramPage page = hologram.getPage(index);
        if (page == null) {
            page = hologram.addPage();
        }

        for (String header : hologramLines.getHeader()) {
            DHAPI.addHologramLine(page, header);
        }

        page.addAction(ClickType.LEFT, new Action(ActionType.NEXT_PAGE, hologram.getName()));
        page.addAction(ClickType.RIGHT, new Action(ActionType.PREV_PAGE, hologram.getName()));

        List<String> playerLines = hologramLines.getLines();
        String emptyLine = hologramLines.getEmptyLine();

        List<HologramLine> lines = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            HologramLine line = DHAPI.addHologramLine(page, emptyLine);
            lines.add(line);
        }

        for (String footer : hologramLines.getFooter()) {
            DHAPI.addHologramLine(page, footer);
        }

        Runnable runnable = () -> {
            List<TopPlayer> monthTopPlayers = supplier.get();

            for (int i = 0; i < monthTopPlayers.size(); i++) {
                TopPlayer topPlayer = monthTopPlayers.get(i);

                String line;
                if (i >= playerLines.size()) {
                    line = playerLines.get(playerLines.size() - 1);
                } else {
                    line = playerLines.get(i);
                }

                line = line.replace("{player}", topPlayer.getNickname())
                        .replace("{votes}", String.valueOf(topPlayer.getVoteCount()));

                lines.get(i).setContent(line);
            }

            for (int i = monthTopPlayers.size(); i < 10; i++) {
                lines.get(i).setContent(emptyLine);
            }
        };

        runnable.run();
        ratingRequester.onTopUpdate(runnable);
    }

    @Override
    public void createHologram(@NotNull Location location) {
        createHologram(location, true);
    }

    private void createHologram(@NotNull Location location, boolean addToConfig) {
        index++;
        Hologram hologram = DHAPI.createHologram("millida-rating-top-" + index, location);

        hologram.setUpdateInterval(Integer.MAX_VALUE);

        hologram.setAlwaysFacePlayer(hologramsConfig.isAlwaysFacePlayer());
        hologram.setFacing(hologramsConfig.getFacing());

        addPage(0, hologram, hologramsConfig.getMonthLines(), ratingRequester::getMonthTopPlayers);
        addPage(1, hologram, hologramsConfig.getWeekLines(), ratingRequester::getWeekTopPlayers);
        addPage(2, hologram, hologramsConfig.getDayLines(), ratingRequester::getDayTopPlayers);

        ratingRequester.onTopUpdate(() -> {
            hologram.updateAll();
        });

        if (addToConfig) {
            FileConfiguration config = plugin.getConfig("holograms.yml");

            ConfigurationSection rawLocations = config.getConfigurationSection("locations");

            ConfigurationSection rawLocation = new MemoryConfiguration();
            rawLocation.set("world", location.getWorld().getName());
            rawLocation.set("x", location.getX());
            rawLocation.set("y", location.getY());
            rawLocation.set("z", location.getZ());

            int i = 0;
            String key;
            do {
                i++;
                key = "created-" + i;
            } while (rawLocations.contains(key));

            rawLocations.set(key, rawLocation);

            saveConfig(config);
        }
    }

    @Override
    public void deleteHolograms(@NotNull Location location) {
        FileConfiguration config = plugin.getConfig("holograms.yml");

        boolean changed = false;
        for (Hologram hologram : holograms) {
            if (hologram.getLocation().distance(location) > 3) {
                continue;
            }

            hologram.delete();

            ConfigurationSection rawLocations = config.getConfigurationSection("locations");
            for (String key : rawLocations.getKeys(false)) {
                Location holoLocation;
                try {
                    holoLocation = ConfigUtils.getLocation(rawLocations, key);
                } catch (ConfigLoadException e) {
                    continue;
                }

                if (holoLocation.equals(hologram.getLocation())) {
                    rawLocations.set(key, null);
                    changed = true;
                    break;
                }
            }
        }

        saveConfig(config);
    }

    private void saveConfig(@NotNull FileConfiguration config) {
        try {
            config.save(new File(plugin.getDataFolder(), "holograms.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        for (Hologram hologram : holograms) {
            hologram.delete();
        }
    }
}
