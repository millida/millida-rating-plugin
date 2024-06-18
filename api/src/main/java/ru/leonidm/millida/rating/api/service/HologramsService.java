package ru.leonidm.millida.rating.api.service;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public interface HologramsService {

    void initialize();

    void createHologram(@NotNull Location location);

    boolean deleteHolograms(@NotNull Location location);

    void close();

}
