package ru.leonidm.millida.rating.service;

import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.api.service.HologramsService;

public final class ProxyHologramsService implements HologramsService {

    @Delegate
    private HologramsService hologramsService;

    public ProxyHologramsService(@NotNull HologramsService hologramService) {
        hologramsService = hologramService;
    }

    public void setHologramService(@NotNull HologramsService hologramService) {
        hologramsService = hologramService;
    }
}
