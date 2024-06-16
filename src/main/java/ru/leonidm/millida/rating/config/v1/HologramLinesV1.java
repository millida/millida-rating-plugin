package ru.leonidm.millida.rating.config.v1;

import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.config.ConfigLoadException;
import ru.leonidm.millida.rating.config.ConfigUtils;
import ru.leonidm.millida.rating.config.v1.api.HologramLines;

import java.util.List;

@Data
public class HologramLinesV1 implements HologramLines {

    private final List<String> header;
    private final List<String> lines;
    private final String emptyLine;
    private final List<String> footer;

    public HologramLinesV1(@NotNull ConfigurationSection section) throws ConfigLoadException {
        header = ConfigUtils.getStringList(section, "header");
        lines = ConfigUtils.getStringList(section, "lines");
        if (lines.isEmpty()) {
            throw ConfigUtils.loadException(section, "lines", "must be non-empty");
        }
        emptyLine = ConfigUtils.getString(section, "empty_line");
        footer = ConfigUtils.getStringList(section, "footer");
    }

}
