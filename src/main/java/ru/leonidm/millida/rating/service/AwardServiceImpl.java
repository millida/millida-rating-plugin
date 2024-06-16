package ru.leonidm.millida.rating.service;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.api.event.OfflinePlayerAwardedEvent;
import ru.leonidm.millida.rating.api.event.PlayerAwardedEvent;
import ru.leonidm.millida.rating.api.service.AwardService;
import ru.leonidm.millida.rating.config.v1.api.Reward;
import ru.leonidm.millida.rating.config.v1.api.Rewards;

import java.util.List;

public class AwardServiceImpl implements AwardService {

    private final Plugin plugin;
    private final Rewards rewards;

    public AwardServiceImpl(@NotNull Plugin plugin, @NotNull Rewards rewards) {
        this.plugin = plugin;
        this.rewards = rewards;
    }

    public void awardAll(@NotNull Player player, int day) {
        award(player, day, false);
        award(player, day, true);
    }

    public void award(@NotNull OfflinePlayer offlinePlayer, int day, boolean online) {
        if (!rewards.isEnabled()) {
            return;
        }

        if (!Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                award(offlinePlayer, day, online);
            });
            return;
        }

        Player player = offlinePlayer.getPlayer();
        Bukkit.getPluginManager().callEvent(player != null
                ? new PlayerAwardedEvent(player)
                : new OfflinePlayerAwardedEvent(offlinePlayer));

        List<Reward> dayRewards = rewards.getReward(day);
        for (Reward reward : dayRewards) {
            if (online != reward.isOnline()) {
                continue;
            }

            reward.apply(offlinePlayer, online);
        }
    }
}
