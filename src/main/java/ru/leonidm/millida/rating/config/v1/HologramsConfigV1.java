package ru.leonidm.millida.rating.config.v1;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.config.ConfigLoadException;
import ru.leonidm.millida.rating.config.ConfigUtils;
import ru.leonidm.millida.rating.config.v1.api.HologramLines;
import ru.leonidm.millida.rating.config.v1.api.HologramsConfig;

@Data
public class HologramsConfigV1 implements HologramsConfig {

    private final Location location;
    private final HologramLines monthLines;
    private final HologramLines weekLines;
    private final HologramLines dayLines;
    private final boolean alwaysFacePlayer;
    private final float facing;

    public HologramsConfigV1(@NotNull ConfigurationSection section) throws ConfigLoadException {
        location = ConfigUtils.getLocation(section, "location");

        ConfigurationSection lines = ConfigUtils.getSection(section, "holograms");

        monthLines = new HologramLinesV1(ConfigUtils.getSection(lines, "month"));
        weekLines = new HologramLinesV1(ConfigUtils.getSection(lines, "week"));
        dayLines = new HologramLinesV1(ConfigUtils.getSection(lines, "day"));
        alwaysFacePlayer = ConfigUtils.getBoolean(lines, "always_face_player");
        facing = (float) ConfigUtils.getDouble(lines, "facing");
    }
}
