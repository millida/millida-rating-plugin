package ru.leonidm.millida.rating.config.api;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface HologramsConfig {

    @NotNull
    List<Location> getLocations();

    @NotNull
    HologramLines getMonthLines();

    @NotNull
    HologramLines getWeekLines();

    @NotNull
    HologramLines getDayLines();

    boolean isAlwaysFacePlayer();

    float getFacing();

}
