package ru.leonidm.millida.rating.api.service;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.api.gui.Gui;

public interface GuiService {

    @NotNull
    Gui getRewardsGui();

}
