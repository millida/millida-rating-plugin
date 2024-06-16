package ru.leonidm.millida.rating.config.v1.api;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface HologramsConfig {

    @NotNull
    Location getLocation();

    @NotNull
    HologramLines getMonthLines();

    @NotNull
    HologramLines getWeekLines();

    @NotNull
    HologramLines getDayLines();

    boolean isAlwaysFacePlayer();

    float getFacing();

}
