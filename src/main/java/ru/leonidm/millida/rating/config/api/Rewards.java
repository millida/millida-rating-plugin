package ru.leonidm.millida.rating.config.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public interface Rewards {

    boolean isEnabled();

    @NotNull
    @Unmodifiable
    List<Reward> getReward(int day);

    @NotNull
    GuiConfig getGuiConfig();

}
