package ru.leonidm.millida.rating.integration;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.millida.rating.api.entity.TopPlayer;
import ru.leonidm.millida.rating.api.service.RatingRequester;

import java.util.List;

public class MillidaRatingPlaceholderExpansion extends PlaceholderExpansion {

    private final RatingRequester ratingRequester;

    public MillidaRatingPlaceholderExpansion(@NotNull RatingRequester ratingRequester) {
        this.ratingRequester = ratingRequester;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "millidarating";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "LeonidM";
    }

    @Override
    @NotNull
    public String getVersion() {
        return Bukkit.getPluginManager().getPlugin("MillidaRating").getDescription().getVersion();
    }

    @Override
    @Nullable
    public String onPlaceholderRequest(@Nullable Player player, @NotNull String params) {
        String[] args = params.split("_");

        if (args.length == 0) {
            return null;
        }

        args[0] = args[0].toLowerCase();

        if (args[0].equals("top")) {
            return handleTop(args);
        } else if (args[0].equals("votes")) {
            // TODO: add API
            return null;
        }

        return null;
    }

    private String handleTop(@NotNull String @NotNull [] args) {
        if (args.length != 3 && args.length != 4) {
            return null;
        }

        args[1] = args[1].toLowerCase();

        List<TopPlayer> topPlayers;
        if (args[1].equals("day")) {
            topPlayers = ratingRequester.getDayTopPlayers();
        } else if (args[1].equals("week")) {
            topPlayers = ratingRequester.getWeekTopPlayers();
        } else if (args[1].equals("month")) {
            topPlayers = ratingRequester.getMonthTopPlayers();
        } else {
            return null;
        }

        int index;
        try {
            index = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            return null;
        }

        if (index >= 10) {
            return null;
        }

        if (index >= topPlayers.size()) {
            return "";
        }

        TopPlayer topPlayer = topPlayers.get(index);
        if (args.length == 3) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(topPlayer.getNickname());
            if (offlinePlayer.isOnline() || offlinePlayer.hasPlayedBefore()) {
                return offlinePlayer.getName();
            }

            return topPlayer.getNickname();
        }

        args[3] = args[3].toLowerCase();
        if (args[3].equals("votes")) {
            return String.valueOf(topPlayer.getVoteCount());
        } else {
            return null;
        }
    }
}
