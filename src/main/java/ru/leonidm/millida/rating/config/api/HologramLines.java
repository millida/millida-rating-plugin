package ru.leonidm.millida.rating.config.api;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface HologramLines {

    @NotNull
    List<String> getHeader();

    @NotNull
    List<String> getLines();

    @NotNull
    String getEmptyLine();

    @NotNull
    List<String> getFooter();

}
