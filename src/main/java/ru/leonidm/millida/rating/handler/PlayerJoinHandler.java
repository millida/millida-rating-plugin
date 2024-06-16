package ru.leonidm.millida.rating.handler;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.api.entity.DeferredReward;
import ru.leonidm.millida.rating.api.repository.DeferredRewardRepository;
import ru.leonidm.millida.rating.api.service.AwardService;

import java.util.List;

public final class PlayerJoinHandler implements Listener {

    private final DeferredRewardRepository deferredRewardRepository;
    private final AwardService awardService;

    public PlayerJoinHandler(@NotNull DeferredRewardRepository deferredRewardRepository,
                             @NotNull AwardService awardService) {
        this.deferredRewardRepository = deferredRewardRepository;
        this.awardService = awardService;
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();

        List<DeferredReward> rewards = deferredRewardRepository.getDeferredRewards(player.getUniqueId());
        if (!rewards.isEmpty()) {
            for (DeferredReward reward : rewards) {
                awardService.award(player, reward.getDay(), true);

                deferredRewardRepository.deleteDeferredReward(reward);
            }
        }
    }
}
