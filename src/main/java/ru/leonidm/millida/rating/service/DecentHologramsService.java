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
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.MillidaRatingPlugin;
import ru.leonidm.millida.rating.api.entity.TopPlayer;
import ru.leonidm.millida.rating.api.service.HologramsService;
import ru.leonidm.millida.rating.api.service.RatingRequester;
import ru.leonidm.millida.rating.config.ConfigLoadException;
import ru.leonidm.millida.rating.config.ConfigUtils;
import ru.leonidm.millida.rating.config.api.HologramLines;
import ru.leonidm.millida.rating.config.api.HologramsConfig;

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

        page.addAction(ClickType.LEFT, new Action(CyclicNextPageActionType.INSTANCE, null));
        page.addAction(ClickType.RIGHT, new Action(CyclicPreviousPageActionType.INSTANCE, null));

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
        Hologram hologram = DHAPI.createHologram("millida-rating-top-" + index, location, false);
        holograms.add(hologram);

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
    public boolean deleteHolograms(@NotNull Location location) {
        FileConfiguration config = plugin.getConfig("holograms.yml");

        for (Hologram hologram : holograms) {
            Location hologramLocation = hologram.getLocation();
            if (location.getWorld() == hologramLocation.getWorld() && hologramLocation.distance(location) > 15) {
                continue;
            }

            hologram.delete();
            holograms.remove(hologram);

            ConfigurationSection rawLocations = config.getConfigurationSection("locations");
            for (String key : rawLocations.getKeys(false)) {
                Location holoLocation;
                try {
                    holoLocation = ConfigUtils.getLocation(rawLocations, key);
                } catch (ConfigLoadException e) {
                    continue;
                }

                if (holoLocation.distance(hologramLocation) < 0.125) {
                    rawLocations.set(key, null);
                    saveConfig(config);
                    return true;
                }
            }
        }

        return false;
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

        holograms.clear();
    }

    public static class CyclicNextPageActionType extends ActionType {

        public static final CyclicNextPageActionType INSTANCE = new CyclicNextPageActionType("CYCLIC_PREV_PAGE");

        private CyclicNextPageActionType(@NotNull String name) {
            super(name);
        }

        @Override
        public boolean execute(Player player, String... args) {
            if (args == null || args.length == 0) {
                return true;
            }
            Hologram hologram = Hologram.getCachedHologram(args[0]);
            if (hologram == null) {
                return true;
            }
            int nextPage = hologram.getPlayerPage(player) + 1;
            if (nextPage < 0 || hologram.size() <= nextPage) {
                nextPage = 0;
            }
            hologram.show(player, nextPage);
            return true;
        }
    }

    public static class CyclicPreviousPageActionType extends ActionType {

        public static final CyclicPreviousPageActionType INSTANCE = new CyclicPreviousPageActionType("CYCLIC_NEXT_PAGE");

        private CyclicPreviousPageActionType(@NotNull String name) {
            super(name);
        }

        @Override
        public boolean execute(Player player, String... args) {
            if (args == null || args.length == 0) {
                return true;
            }
            Hologram hologram = Hologram.getCachedHologram(args[0]);
            if (hologram == null) {
                return true;
            }
            int prevPage = hologram.getPlayerPage(player) - 1;
            if (prevPage < 0 || hologram.size() <= prevPage) {
                prevPage = hologram.size() - 1;
            }
            hologram.show(player, prevPage);
            return true;
        }
    }
}
