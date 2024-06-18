package ru.leonidm.millida.rating.config.impl;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.config.ConfigLoadException;
import ru.leonidm.millida.rating.config.ConfigUtils;
import ru.leonidm.millida.rating.config.api.HologramLines;
import ru.leonidm.millida.rating.config.api.HologramsConfig;

import java.util.ArrayList;
import java.util.List;

@Data
public class HologramsConfigImpl implements HologramsConfig {

    private final List<Location> locations;
    private final HologramLines monthLines;
    private final HologramLines weekLines;
    private final HologramLines dayLines;
    private final boolean alwaysFacePlayer;
    private final float facing;

    public HologramsConfigImpl(@NotNull ConfigurationSection section) throws ConfigLoadException {
        locations = new ArrayList<>();

        ConfigurationSection rawLocations = ConfigUtils.getSection(section, "locations");
        for (String key : rawLocations.getKeys(false)) {
            locations.add(ConfigUtils.getLocation(rawLocations, key));
        }

        ConfigurationSection lines = ConfigUtils.getSection(section, "holograms");

        monthLines = new HologramLinesImpl(ConfigUtils.getSection(lines, "month"));
        weekLines = new HologramLinesImpl(ConfigUtils.getSection(lines, "week"));
        dayLines = new HologramLinesImpl(ConfigUtils.getSection(lines, "day"));
        alwaysFacePlayer = ConfigUtils.getBoolean(lines, "always_face_player");
        facing = (float) ConfigUtils.getDouble(lines, "facing");
    }
}
