package ru.leonidm.millida.rating.service;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.api.gui.Gui;
import ru.leonidm.millida.rating.api.repository.RatingPlayerRepository;
import ru.leonidm.millida.rating.api.service.GuiService;
import ru.leonidm.millida.rating.config.v1.api.GuiConfig;
import ru.leonidm.millida.rating.config.v1.api.Rewards;
import ru.leonidm.millida.rating.gui.RewardsGui;

public class GuiServiceImpl implements GuiService {

    private final RewardsGui rewardsGui;

    public GuiServiceImpl(@NotNull RatingPlayerRepository repository, @NotNull Rewards rewards,
                          @NotNull GuiConfig guiConfig) {
        rewardsGui = new RewardsGui(repository, rewards, guiConfig);
    }

    @Override
    @NotNull
    public Gui getRewardsGui() {
        return rewardsGui;
    }
}
