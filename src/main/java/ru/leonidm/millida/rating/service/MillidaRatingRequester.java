package ru.leonidm.millida.rating.service;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import ru.leonidm.millida.rating.api.entity.RatingPlayer;
import ru.leonidm.millida.rating.api.entity.TopPlayer;
import ru.leonidm.millida.rating.api.entity.Vote;
import ru.leonidm.millida.rating.api.event.RatingVoteEvent;
import ru.leonidm.millida.rating.api.repository.DeferredRewardRepository;
import ru.leonidm.millida.rating.api.repository.RatingPlayerRepository;
import ru.leonidm.millida.rating.api.repository.StatisticRepository;
import ru.leonidm.millida.rating.api.service.AwardService;
import ru.leonidm.millida.rating.api.service.RatingRequestService;
import ru.leonidm.millida.rating.api.service.RatingRequester;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class MillidaRatingRequester implements RatingRequester {

    private final Logger logger = Logger.getLogger("MillidaRating");
    private final List<Runnable> onTopUpdateRunnables = new ArrayList<>();
    private final RatingRequestService ratingRequestService;
    private final DeferredRewardRepository deferredRewardRepository;
    private final RatingPlayerRepository ratingPlayerRepository;
    private final StatisticRepository statisticRepository;
    private final AwardService awardService;
    private final Plugin plugin;
    private final long requestPeriod;
    private final long topRequestPeriod;
    private BukkitTask bukkitTask;
    private List<TopPlayer> dayTopPlayers = Collections.emptyList();
    private List<TopPlayer> weekTopPlayers = Collections.emptyList();
    private List<TopPlayer> monthTopPlayers = Collections.emptyList();

    public MillidaRatingRequester(@NotNull RatingRequestService ratingRequestService,
                                  @NotNull DeferredRewardRepository deferredRewardRepository,
                                  @NotNull RatingPlayerRepository ratingPlayerRepository,
                                  @NotNull StatisticRepository statisticRepository,
                                  @NotNull AwardService awardService,
                                  @NotNull Plugin plugin, long requestPeriod, long topRequestPeriod) {
        this.ratingRequestService = ratingRequestService;
        this.deferredRewardRepository = deferredRewardRepository;
        this.ratingPlayerRepository = ratingPlayerRepository;
        this.statisticRepository = statisticRepository;
        this.awardService = awardService;
        this.plugin = plugin;
        this.requestPeriod = requestPeriod;
        this.topRequestPeriod = topRequestPeriod;
    }

    @Override
    public void initialize() {
        if (bukkitTask != null) {
            bukkitTask.cancel();
        }

        bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            handleVotes();
        }, 0, requestPeriod * 20);

        bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            handleTop();
        }, 0, topRequestPeriod * 20);
    }

    @NotNull
    @Unmodifiable
    public List<TopPlayer> getDayTopPlayers() {
        return dayTopPlayers;
    }

    @NotNull
    @Unmodifiable
    public List<TopPlayer> getWeekTopPlayers() {
        return weekTopPlayers;
    }

    @NotNull
    @Unmodifiable
    public List<TopPlayer> getMonthTopPlayers() {
        return monthTopPlayers;
    }

    @Override
    public void onTopUpdate(@NotNull Runnable runnable) {
        onTopUpdateRunnables.add(runnable);
    }

    private void handleTop() {
        dayTopPlayers = ratingRequestService.topDay();
        monthTopPlayers = ratingRequestService.topMonth();
        weekTopPlayers = ratingRequestService.topWeek();

        Bukkit.getScheduler().runTask(plugin, () -> {
            onTopUpdateRunnables.forEach(Runnable::run);
        });
    }

    private void handleVotes() {
        long lastVote = statisticRepository.getLastVote();

        List<Vote> newVotes = new ArrayList<>();

        // TODO: fix API so there will be empty pages
        List<Vote> previousVotes = null;

        votes:
        for (int i = 1; ; i++) {
            List<Vote> votes = ratingRequestService.fetch(i);
            if (votes.isEmpty() || votes.equals(previousVotes)) {
                break;
            }

            previousVotes = votes;

            for (Vote vote : votes) {
                if (vote.getVoteId() > lastVote) {
                    newVotes.add(vote);
                } else {
                    break votes;
                }
            }
        }

        if (newVotes.isEmpty()) {
            return;
        }

        long newLastVote = lastVote;
        try {
            Collections.reverse(newVotes);

            for (Vote vote : new LinkedHashSet<>(newVotes)) {
                String nickname = vote.getNickname();

                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(nickname);
                if (offlinePlayer == null || (!offlinePlayer.isOnline() && !offlinePlayer.hasPlayedBefore())) {
                    logger.warning("Unknown player '" + nickname + "' voted, ignored their vote");
                    continue;
                }

                Bukkit.getPluginManager().callEvent(new RatingVoteEvent(vote, offlinePlayer));

                UUID uuid = offlinePlayer.getUniqueId();
                RatingPlayer ratingPlayer = ratingPlayerRepository.findRatingPlayer(uuid);
                if (ratingPlayer == null) {
                    ratingPlayer = ratingPlayerRepository.createRatingPlayer(uuid);
                    if (ratingPlayer == null) {
                        logger.warning("Player '" + nickname + "' was not registered, ignored their vote");
                        continue;
                    }
                }

                boolean newStreak;

                if (ratingPlayer.getLastVoteTimestamp() > 0) {
                    Instant lastInstant = Instant.ofEpochMilli(ratingPlayer.getLastVoteTimestamp());
                    Instant instant = Instant.ofEpochMilli(vote.getVoteTimestamp() * 1000);

                    LocalDateTime lastDate = LocalDateTime.ofInstant(lastInstant, ZoneOffset.UTC);
                    LocalDateTime date = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);

                    Month lastMonth = lastDate.getMonth();
                    Month month = date.getMonth();

                    int lastYear = lastDate.getYear();
                    int year = date.getYear();

                    newStreak = !lastMonth.equals(month) || lastYear != year;
                } else {
                    newStreak = true;
                }

                int day;
                if (newStreak) {
                    day = 1;
                } else {
                    day = ratingPlayer.getStreak() + 1;
                }

                Player player = offlinePlayer.getPlayer();
                if (player != null) {
                    awardService.awardAll(player, day);
                } else {
                    deferredRewardRepository.addDeferredReward(uuid, day);
                    awardService.award(offlinePlayer, day, false);
                }

                newLastVote = vote.getVoteId();

                ratingPlayer.setStreak(day);
                ratingPlayer.setLastVoteTimestamp(vote.getVoteTimestamp() * 1000);
                ratingPlayerRepository.saveRatingPlayer(ratingPlayer);
            }
        } finally {
            if (lastVote != newLastVote) {
                statisticRepository.setLastVote(newLastVote);
            }
        }
    }

    @Override
    public void close() {
        if (bukkitTask != null) {
            bukkitTask.cancel();
            bukkitTask = null;
        }
    }
}
