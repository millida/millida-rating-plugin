package ru.leonidm.millida.rating.service;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.actions.Action;
import eu.decentsoftware.holograms.api.actions.ActionType;
import eu.decentsoftware.holograms.api.actions.ClickType;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.MillidaRatingPlugin;
import ru.leonidm.millida.rating.api.entity.TopPlayer;
import ru.leonidm.millida.rating.api.service.HologramsService;
import ru.leonidm.millida.rating.api.service.RatingRequester;
import ru.leonidm.millida.rating.config.v1.api.HologramLines;
import ru.leonidm.millida.rating.config.v1.api.HologramsConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DecentHologramsService implements HologramsService {

    private final MillidaRatingPlugin plugin;
    private final HologramsConfig hologramsConfig;
    private final RatingRequester ratingRequester;
    private Hologram hologram;

    public DecentHologramsService(@NotNull MillidaRatingPlugin plugin, @NotNull HologramsConfig hologramsConfig,
                                  @NotNull RatingRequester ratingRequester) {
        this.plugin = plugin;
        this.hologramsConfig = hologramsConfig;
        this.ratingRequester = ratingRequester;
    }

    @Override
    public void initialize() {
        hologram = DHAPI.createHologram("millida-rating-top", hologramsConfig.getLocation());

        hologram.setUpdateInterval(Integer.MAX_VALUE);

        hologram.setAlwaysFacePlayer(hologramsConfig.isAlwaysFacePlayer());
        hologram.setFacing(hologramsConfig.getFacing());

        addPage(0, hologramsConfig.getMonthLines(), ratingRequester::getMonthTopPlayers);
        addPage(1, hologramsConfig.getWeekLines(), ratingRequester::getWeekTopPlayers);
        addPage(2, hologramsConfig.getDayLines(), ratingRequester::getDayTopPlayers);

        ratingRequester.onTopUpdate(() -> {
            hologram.updateAll();
        });
    }

    @Override
    public void teleportHologram(@NotNull Location location) {
        if (hologram != null) {
            hologram.setLocation(location);
            hologram.updateAll();

            FileConfiguration config = plugin.getConfig("holograms.yml");
            config.set("location.world", location.getWorld().getName());
            config.set("location.x", location.getX());
            config.set("location.y", location.getY());
            config.set("location.z", location.getZ());

            try {
                config.save(new File(plugin.getDataFolder(), "holograms.yml"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addPage(int index, @NotNull HologramLines hologramLines, @NotNull Supplier<List<TopPlayer>> supplier) {
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
    public void close() {
        if (hologram != null) {
            hologram.delete();
        }
    }
}
