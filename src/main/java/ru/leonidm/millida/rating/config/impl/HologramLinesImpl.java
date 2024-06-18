package ru.leonidm.millida.rating.config.impl;

import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.config.ConfigLoadException;
import ru.leonidm.millida.rating.config.ConfigUtils;
import ru.leonidm.millida.rating.config.api.HologramLines;

import java.util.List;

@Data
public class HologramLinesImpl implements HologramLines {

    private final List<String> header;
    private final List<String> lines;
    private final String emptyLine;
    private final List<String> footer;

    public HologramLinesImpl(@NotNull ConfigurationSection section) throws ConfigLoadException {
        header = ConfigUtils.getStringList(section, "header");
        lines = ConfigUtils.getStringList(section, "lines");
        if (lines.isEmpty()) {
            throw ConfigUtils.loadException(section, "lines", "must be non-empty");
        }
        emptyLine = ConfigUtils.getString(section, "empty_line");
        footer = ConfigUtils.getStringList(section, "footer");
    }

}
