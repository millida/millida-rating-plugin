package ru.leonidm.millida.rating;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.api.MillidaRatingApi;
import ru.leonidm.millida.rating.command.RatingCommand;
import ru.leonidm.millida.rating.command.RewardsCommand;
import ru.leonidm.millida.rating.config.ConfigLoadException;
import ru.leonidm.millida.rating.config.ConfigLoader;
import ru.leonidm.millida.rating.config.v1.api.Config;
import ru.leonidm.millida.rating.config.v1.api.ConnectionFactory;
import ru.leonidm.millida.rating.config.v1.api.HologramsConfig;
import ru.leonidm.millida.rating.config.v1.api.MessagesConfig;
import ru.leonidm.millida.rating.handler.GuiHandler;
import ru.leonidm.millida.rating.handler.PlayerJoinHandler;
import ru.leonidm.millida.rating.integration.MillidaRatingPlaceholderExpansion;
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

import java.io.File;
import java.util.logging.Logger;

public final class MillidaRatingPlugin extends JavaPlugin implements MillidaRatingApi {

    private ProxyRatingRequestService ratingRequestService;
    private ProxyDeferredRewardRepository deferredRewardRepository;
    private ProxyRatingPlayerRepository ratingPlayerRepository;
    private ProxyStatisticRepository statisticRepository;
    private ProxyAwardService awardService;
    private ProxyRatingRequester ratingRequester;
    private ProxyGuiService guiService;
    private ProxyHologramsService hologramsService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Config config;

        Logger logger = getLogger();
        PluginManager pluginManager = Bukkit.getPluginManager();
        try {
            FileConfiguration fileConfiguration = getConfig();
            fileConfiguration.setDefaults(new MemoryConfiguration());
            config = ConfigLoader.load(this, fileConfiguration);
        } catch (Throwable t) {
            if (t instanceof ConfigLoadException) {
                logger.severe("Config is not valid! " + t.getMessage());
            } else {
                logger.severe("Got exception while loading config");
                t.printStackTrace();
            }

            logger.severe(">>> Disabling the plugin");
            pluginManager.disablePlugin(this);
            return;
        }

        ratingRequestService = new ProxyRatingRequestService(new MillidaRatingRequestService(config.getServerId()));
//        ratingRequestService = new ProxyRatingRequestService(new MillidaRatingRequestService("http://127.0.0.1:5000/"));

        ConnectionFactory database = config.getDatabase();
        deferredRewardRepository = new ProxyDeferredRewardRepository(database.createDeferredRewardRepository());
        deferredRewardRepository.initialize();

        ratingPlayerRepository = new ProxyRatingPlayerRepository(database.createRatingPlayerRepository());
        ratingPlayerRepository.initialize();

        statisticRepository = new ProxyStatisticRepository(new FileStatisticRepository(this));
        statisticRepository.initialize();

        awardService = new ProxyAwardService(new AwardServiceImpl(this, config.getRewards()));

        ratingRequester = new ProxyRatingRequester(new MillidaRatingRequester(
                ratingRequestService, deferredRewardRepository, ratingPlayerRepository, statisticRepository,
                awardService, this, config.getRequestPeriod(), config.getTopRequestPeriod()
        ));
        ratingRequester.initialize();

        guiService = new ProxyGuiService(new GuiServiceImpl(
                ratingPlayerRepository, config.getRewards(), config.getRewards().getGuiConfig()
        ));

        if (pluginManager.isPluginEnabled("DecentHolograms")) {
            FileConfiguration fileConfiguration = getConfig("holograms.yml");
            HologramsConfig hologramsConfig = null;
            try {
                hologramsConfig = ConfigLoader.loadHolograms(this, fileConfiguration);
            } catch (Throwable t) {
                if (t instanceof ConfigLoadException) {
                    logger.severe("Holograms config is not valid! " + t.getMessage());
                } else {
                    logger.severe("Got exception while loading holograms config");
                    t.printStackTrace();
                }

                logger.severe(">>> Disabling holograms module");
            }

            if (hologramsConfig != null) {
                hologramsService = new ProxyHologramsService(new DecentHologramsService(this, hologramsConfig, ratingRequester));
                hologramsService.initialize();
            }
        }

        if (hologramsService == null) {
            hologramsService = new ProxyHologramsService(new DisabledHologramsService());
        }

        if (pluginManager.isPluginEnabled("PlaceholderAPI")) {
            new MillidaRatingPlaceholderExpansion(ratingRequester).register();
        }

        pluginManager.registerEvents(new PlayerJoinHandler(deferredRewardRepository, awardService), this);
        pluginManager.registerEvents(new GuiHandler(), this);

        String commandName = config.getRewards().getGuiConfig().getCommand();
        PluginCommand command = getCommand(commandName);
        if (command == null) {
            logger.severe("Cannot find command '" + commandName + "' (did you change config.yml without plugin.yml?)");
            logger.severe(">>> Disabling GUI module");
        } else {
            command.setExecutor(new RewardsCommand(guiService));
        }

        command = getCommand("rating");
        if (command == null) {
            logger.severe("Cannot find command 'rating' (did you change incorrectly plugin.yml?)");
            logger.severe(">>> Disabling command module");
        } else {
            FileConfiguration fileConfiguration = getConfig("messages.yml");
            MessagesConfig messagesConfig = null;
            try {
                messagesConfig = ConfigLoader.loadMessages(this, fileConfiguration);
            } catch (Throwable t) {
                if (t instanceof ConfigLoadException) {
                    logger.severe("Messages config is not valid! " + t.getMessage());
                } else {
                    logger.severe("Got exception while messages holograms config");
                    t.printStackTrace();
                }

                logger.severe(">>> Disabling command module");
            }

            if (messagesConfig != null) {
                command.setExecutor(new RatingCommand(this, messagesConfig).build());
            }
        }

        logger.info("Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabled!");
    }

    @NotNull
    public FileConfiguration getConfig(@NotNull String name) {
        File configFile = new File(getDataFolder(), name);
        if (!configFile.exists()) {
            saveResource(name, false);
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }

    @Override
    @NotNull
    public ProxyRatingRequestService getRatingRequestService() {
        return ratingRequestService;
    }

    @Override
    @NotNull
    public ProxyDeferredRewardRepository getDeferredRewardRepository() {
        return deferredRewardRepository;
    }

    @Override
    @NotNull
    public ProxyRatingPlayerRepository getRatingPlayerRepository() {
        return ratingPlayerRepository;
    }

    @Override
    @NotNull
    public ProxyStatisticRepository getStatisticRepository() {
        return statisticRepository;
    }

    @Override
    @NotNull
    public ProxyAwardService getAwardService() {
        return awardService;
    }

    @Override
    @NotNull
    public ProxyRatingRequester getRatingRequester() {
        return ratingRequester;
    }

    @Override
    @NotNull
    public ProxyGuiService getGuiService() {
        return guiService;
    }

    @Override
    @NotNull
    public ProxyHologramsService getHologramsService() {
        return hologramsService;
    }
}
