package ru.leonidm.millida.rating.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.MillidaRatingPlugin;
import ru.leonidm.millida.rating.api.entity.DeferredReward;
import ru.leonidm.millida.rating.api.entity.RatingPlayer;
import ru.leonidm.millida.rating.api.repository.DeferredRewardRepository;
import ru.leonidm.millida.rating.api.repository.RatingPlayerRepository;
import ru.leonidm.millida.rating.api.service.AwardService;
import ru.leonidm.millida.rating.config.ConfigLoadException;
import ru.leonidm.millida.rating.config.ConfigLoader;
import ru.leonidm.millida.rating.config.v1.api.AdminCommandMessagesConfig;
import ru.leonidm.millida.rating.config.v1.api.Config;
import ru.leonidm.millida.rating.config.v1.api.ConnectionFactory;
import ru.leonidm.millida.rating.config.v1.api.GuiConfig;
import ru.leonidm.millida.rating.config.v1.api.HologramsConfig;
import ru.leonidm.millida.rating.config.v1.api.MessagesConfig;
import ru.leonidm.millida.rating.config.v1.api.Rewards;
import ru.leonidm.millida.rating.external.command.CommandSpec;
import ru.leonidm.millida.rating.external.command.CommandSpecExecutor;
import ru.leonidm.millida.rating.repository.FileStatisticRepository;
import ru.leonidm.millida.rating.repository.ProxyStatisticRepository;
import ru.leonidm.millida.rating.repository.player.ProxyRatingPlayerRepository;
import ru.leonidm.millida.rating.repository.reward.ProxyDeferredRewardRepository;
import ru.leonidm.millida.rating.service.AwardServiceImpl;
import ru.leonidm.millida.rating.service.DecentHologramsService;
import ru.leonidm.millida.rating.service.DisabledHologramsService;
import ru.leonidm.millida.rating.service.GuiServiceImpl;
import ru.leonidm.millida.rating.service.MillidaRatingRequestService;
import ru.leonidm.millida.rating.service.MillidaRatingRequester;
import ru.leonidm.millida.rating.service.ProxyAwardService;
import ru.leonidm.millida.rating.service.ProxyGuiService;
import ru.leonidm.millida.rating.service.ProxyHologramsService;
import ru.leonidm.millida.rating.service.ProxyRatingRequestService;
import ru.leonidm.millida.rating.service.ProxyRatingRequester;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class RatingCommand {

    private final MillidaRatingPlugin plugin;
    private final MessagesConfig messagesConfig;
    private final AdminCommandMessagesConfig adminCommandMessagesConfig;

    public RatingCommand(@NotNull MillidaRatingPlugin plugin, @NotNull MessagesConfig messagesConfig) {
        this.plugin = plugin;
        this.messagesConfig = messagesConfig;
        adminCommandMessagesConfig = messagesConfig.getCommandsMessagesConfig().getAdminCommandMessagesConfig();
    }

    @NotNull
    public CommandSpec build() {
        CommandSpecExecutor openGuiExecutor = (sender, args) -> {
            if (sender instanceof Player) {
                plugin.getGuiService().getRewardsGui().openInventory((Player) sender);
            }
        };

        return CommandSpec.builder()
                .permission("millida.rating.command")
                .child(admin(), "admin")
                .child(CommandSpec.builder()
                        .permission("millida.rating.command.rewards")
                        .executor(openGuiExecutor)
                        .build(), "rewards")
                .executor(openGuiExecutor)
                .build();
    }

    @NotNull
    private CommandSpec admin() {
        CommandSpecExecutor usageExecutor = (sender, args) -> {
            sendMessage(sender, adminCommandMessagesConfig.getUsage());
        };

        CommandSpec.Builder builder = CommandSpec.builder()
                .permission("millida.rating.command.admin")
                .executor(usageExecutor)
                .child(CommandSpec.builder()
                        .executor(usageExecutor)
                        .child(CommandSpec.builder()
                                .executor((sender, args) -> {
                                    reloadAll(sender);
                                    sendMessage(sender, messagesConfig.getCommandsMessagesConfig().getOk());
                                })
                                .build(), "*")
                        .child(CommandSpec.builder()
                                .executor((sender, args) -> {
                                    reloadHolograms(sender);
                                    sendMessage(sender, messagesConfig.getCommandsMessagesConfig().getOk());
                                })
                                .build(), "holograms")
                        .child(CommandSpec.builder()
                                .executor((sender, args) -> {
                                    reloadGui(sender);
                                    sendMessage(sender, messagesConfig.getCommandsMessagesConfig().getOk());
                                })
                                .build(), "gui")
                        .executor((sender, args) -> {
                            reloadAll(sender);
                        })
                        .build(), "reload")
                .child(CommandSpec.builder()
                        .executor((sender, args) -> {
                            // TODO: separate clear rewards & players
                            plugin.getDeferredRewardRepository().clear();
                            plugin.getRatingPlayerRepository().clear();
                            sendMessage(sender, messagesConfig.getCommandsMessagesConfig().getOk());
                        })
                        .build(), "clearrewards")
                .child(CommandSpec.builder()
                        .executor((sender, args) -> {
                            if (args.length != 1) {
                                sendMessage(sender, adminCommandMessagesConfig.getUsage());
                                return;
                            }

                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
                            if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) {
                                sendMessage(sender, adminCommandMessagesConfig.getUnknownPlayer()
                                        .replace("{player}", args[0]));
                                return;
                            }

                            UUID uuid = offlinePlayer.getUniqueId();

                            DeferredRewardRepository rewardRepository = plugin.getDeferredRewardRepository();
                            List<DeferredReward> rewards = rewardRepository.getDeferredRewards(uuid);

                            for (DeferredReward reward : rewards) {
                                rewardRepository.deleteDeferredReward(reward);
                            }

                            RatingPlayerRepository playerRepository = plugin.getRatingPlayerRepository();
                            playerRepository.deleteRatingPlayer(uuid);

                            sendMessage(sender, messagesConfig.getCommandsMessagesConfig().getOk());
                        })
                        .completer((sender, args) -> args.length == 1 ? null : Collections.emptyList())
                        .build(), "clear")
                .child(CommandSpec.builder()
                        .executor((sender, args) -> {
                            if (args.length != 1) {
                                sendMessage(sender, adminCommandMessagesConfig.getUsage());
                                return;
                            }

                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
                            if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) {
                                sendMessage(sender, adminCommandMessagesConfig.getUnknownPlayer()
                                        .replace("{player}", args[0]));
                                return;
                            }

                            UUID uuid = offlinePlayer.getUniqueId();

                            RatingPlayerRepository ratingPlayerRepository = plugin.getRatingPlayerRepository();
                            RatingPlayer ratingPlayer = ratingPlayerRepository.findRatingPlayer(uuid);
                            if (ratingPlayer == null) {
                                ratingPlayer = ratingPlayerRepository.createRatingPlayer(uuid);
                                if (ratingPlayer == null) {
                                    sendMessage(sender, adminCommandMessagesConfig.getUnknownPlayer()
                                            .replace("{player}", args[0]));
                                    return;
                                }
                            }

                            AwardService awardService = plugin.getAwardService();
                            DeferredRewardRepository deferredRewardRepository = plugin.getDeferredRewardRepository();

                            int day = ratingPlayer.getStreak() + 1;

                            Player player = offlinePlayer.getPlayer();
                            if (player != null) {
                                awardService.awardAll(player, day);
                            } else {
                                deferredRewardRepository.addDeferredReward(uuid, day);
                                awardService.award(offlinePlayer, day, false);
                            }

                            ratingPlayer.setStreak(day);
                            ratingPlayerRepository.saveRatingPlayer(ratingPlayer);

                            sendMessage(sender, messagesConfig.getCommandsMessagesConfig().getOk());
                        })
                        .completer((sender, args) -> args.length == 1 ? null : Collections.emptyList())
                        .build(), "vote");

        if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
            builder.child(hologram(), "hologram");
        }

        return builder
                .build();
    }

    @NotNull
    private CommandSpec hologram() {
        return CommandSpec.builder()
                .child(CommandSpec.builder()
                        .executor((sender, args) -> {
                            if (sender instanceof Player) {
                                ProxyHologramsService hologramsService = plugin.getHologramsService();
                                hologramsService.createHologram(((Player) sender).getLocation());
                                sendMessage(sender, messagesConfig.getCommandsMessagesConfig().getOk());
                            }
                        })
                        .build(), "create")
                .child(CommandSpec.builder()
                        .executor((sender, args) -> {
                            if (sender instanceof Player) {
                                ProxyHologramsService hologramsService = plugin.getHologramsService();
                                hologramsService.deleteHolograms(((Player) sender).getLocation());
                                sendMessage(sender, messagesConfig.getCommandsMessagesConfig().getOk());
                            }
                        })
                        .build(), "delete")
                .build();
    }

    private void reloadHolograms(@NotNull CommandSender sender) {
        ProxyHologramsService hologramsService = plugin.getHologramsService();
        hologramsService.close();

        Logger logger = plugin.getLogger();
        if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
            FileConfiguration fileConfiguration = plugin.getConfig("holograms.yml");
            HologramsConfig hologramsConfig = null;
            try {
                hologramsConfig = ConfigLoader.loadHolograms(plugin, fileConfiguration);
            } catch (Throwable t) {
                if (t instanceof ConfigLoadException) {
                    logger.severe("Holograms config is not valid! " + t.getMessage());
                } else {
                    logger.severe("Got exception while loading holograms config");
                    t.printStackTrace();
                }

                logger.severe(">>> Disabling holograms module");
                sendMessage(sender, adminCommandMessagesConfig.getDisablingModule());
            }

            if (hologramsConfig != null) {
                hologramsService.setHologramService(new DecentHologramsService(plugin, hologramsConfig, plugin.getRatingRequester()));
                hologramsService.initialize();
                return;
            }
        }

        hologramsService.setHologramService(new DisabledHologramsService());
    }

    private void reloadGui(@NotNull CommandSender sender) {
        Config config;

        Logger logger = plugin.getLogger();
        try {
            plugin.reloadConfig();
            FileConfiguration fileConfiguration = plugin.getConfig();
            fileConfiguration.setDefaults(new MemoryConfiguration());
            config = ConfigLoader.load(plugin, fileConfiguration);
        } catch (Throwable t) {
            if (t instanceof ConfigLoadException) {
                logger.severe("Config is not valid! " + t.getMessage());
            } else {
                logger.severe("Got exception while loading config");
                t.printStackTrace();
            }

            sendMessage(sender, adminCommandMessagesConfig.getDisablingModule());
            return;
        }

        ProxyRatingPlayerRepository ratingPlayerRepository = plugin.getRatingPlayerRepository();
        Rewards rewards = config.getRewards();
        GuiConfig guiConfig = rewards.getGuiConfig();
        plugin.getGuiService().setGuiService(new GuiServiceImpl(ratingPlayerRepository, rewards, guiConfig));
    }

    private void reloadMessages(@NotNull CommandSender sender) {
        Logger logger = plugin.getLogger();
        PluginCommand command = plugin.getCommand("rating");
        if (command == null) {
            logger.severe("Cannot find command 'rating' (did you change incorrectly plugin.yml?)");
            logger.severe(">>> Disabling command module");
            sendMessage(sender, adminCommandMessagesConfig.getDisablingModule());
        } else {
            FileConfiguration fileConfiguration = plugin.getConfig("messages.yml");
            MessagesConfig messagesConfig = null;
            try {
                messagesConfig = ConfigLoader.loadMessages(plugin, fileConfiguration);
            } catch (Throwable t) {
                if (t instanceof ConfigLoadException) {
                    logger.severe("Messages config is not valid! " + t.getMessage());
                } else {
                    logger.severe("Got exception while messages holograms config");
                    t.printStackTrace();
                }

                logger.severe(">>> Disabling command module");
                sendMessage(sender, adminCommandMessagesConfig.getDisablingModule());
            }

            if (messagesConfig != null) {
                command.setExecutor(new RatingCommand(plugin, messagesConfig).build());
            }
        }
    }

    private void reloadAll(@NotNull CommandSender sender) {
        Config config;

        Logger logger = plugin.getLogger();
        try {
            FileConfiguration fileConfiguration = plugin.getConfig();
            fileConfiguration.setDefaults(new MemoryConfiguration());
            config = ConfigLoader.load(plugin, fileConfiguration);
        } catch (Throwable t) {
            if (t instanceof ConfigLoadException) {
                logger.severe("Config is not valid! " + t.getMessage());
            } else {
                logger.severe("Got exception while loading config");
                t.printStackTrace();
            }

            sendMessage(sender, adminCommandMessagesConfig.getDisablingModule());
            return;
        }

        plugin.getDeferredRewardRepository().close();
        plugin.getRatingPlayerRepository().close();
        plugin.getStatisticRepository().close();
        plugin.getRatingRequester().close();

        try {
            ProxyRatingRequestService ratingRequestService = plugin.getRatingRequestService();
            ratingRequestService.setRatingRequestService(new MillidaRatingRequestService(config.getServerId()));

            ConnectionFactory database = config.getDatabase();

            ProxyDeferredRewardRepository deferredRewardRepository = plugin.getDeferredRewardRepository();
            deferredRewardRepository.setDeferredRewardRepository(database.createDeferredRewardRepository());
            deferredRewardRepository.initialize();

            ProxyRatingPlayerRepository ratingPlayerRepository = plugin.getRatingPlayerRepository();
            ratingPlayerRepository.setRatingPlayerRepository(database.createRatingPlayerRepository());
            ratingPlayerRepository.initialize();

            ProxyStatisticRepository statisticRepository = plugin.getStatisticRepository();
            statisticRepository.setStatisticRepository(new FileStatisticRepository(plugin));
            statisticRepository.initialize();

            ProxyAwardService awardService = plugin.getAwardService();
            awardService.setAwardService(new AwardServiceImpl(plugin, config.getRewards()));

            ProxyRatingRequester ratingRequester = plugin.getRatingRequester();
            ratingRequester.setRatingRequester(new MillidaRatingRequester(
                    ratingRequestService, deferredRewardRepository, ratingPlayerRepository, statisticRepository,
                    awardService, plugin, config.getRequestPeriod(), config.getTopRequestPeriod()
            ));
            ratingRequester.initialize();

            ProxyGuiService guiService = plugin.getGuiService();
            guiService.setGuiService(new GuiServiceImpl(
                    ratingPlayerRepository, config.getRewards(), config.getRewards().getGuiConfig()
            ));

            reloadHolograms(sender);
            reloadGui(sender);
            reloadMessages(sender);
        } catch (Exception e) {
            logger.severe("Cannot register new services");
            e.printStackTrace();
            logger.severe(">>> Disabling the plugin");
            sendMessage(sender, adminCommandMessagesConfig.getDisablingPlugin());

            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    private void sendMessage(@NotNull CommandSender sender, @NotNull String message) {
        sender.sendMessage(messagesConfig.getPrefix() + message);
    }
}
