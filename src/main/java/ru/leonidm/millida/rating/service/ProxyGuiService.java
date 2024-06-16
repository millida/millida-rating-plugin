package ru.leonidm.millida.rating.service;

import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.api.service.GuiService;

public class ProxyGuiService implements GuiService {

    @Delegate
    private GuiService guiService;

    public ProxyGuiService(@NotNull GuiService guiService) {
        this.guiService = guiService;
    }

    public void setGuiService(@NotNull GuiService guiService) {
        this.guiService = guiService;
    }
}
