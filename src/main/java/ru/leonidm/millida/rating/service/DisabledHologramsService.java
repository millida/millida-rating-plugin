package ru.leonidm.millida.rating.service;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.api.service.HologramsService;

public class DisabledHologramsService implements HologramsService {

    @Override
    public void initialize() {

    }

    @Override
    public void createHologram(@NotNull Location location) {

    }

    @Override
    public boolean deleteHolograms(@NotNull Location location) {
        return false;
    }

    @Override
    public void close() {

    }
}
