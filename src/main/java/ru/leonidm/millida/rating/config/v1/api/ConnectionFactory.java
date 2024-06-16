package ru.leonidm.millida.rating.config.v1.api;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.api.repository.DeferredRewardRepository;
import ru.leonidm.millida.rating.api.repository.RatingPlayerRepository;
import ru.leonidm.millida.rating.config.ConfigLoadException;
import ru.leonidm.millida.rating.config.ConfigUtils;
import ru.leonidm.millida.rating.repository.player.MySqlRatingPlayerRepository;
import ru.leonidm.millida.rating.repository.player.SqliteRatingPlayerRepository;
import ru.leonidm.millida.rating.repository.reward.MySqlDeferredRewardRepository;
import ru.leonidm.millida.rating.repository.reward.SqliteDeferredRewardRepository;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public interface ConnectionFactory {

    @NotNull
    static ConnectionFactory from(@NotNull Plugin plugin, @NotNull ConfigurationSection section)
            throws ConfigLoadException {
        String type = ConfigUtils.getString(section, "type");
        switch (type.toLowerCase()) {
            case "sqlite":
                return new SqliteConnectionFactory(plugin, section);
            case "mysql":
                return new MysqlConnectionFactory(section);
            default:
                throw new ConfigLoadException("Unknown value '" + type + "' of " +
                        section.getCurrentPath() + ".type");
        }
    }

    @NotNull
    Connection openConnection() throws SQLException;

    @NotNull
    DeferredRewardRepository createDeferredRewardRepository();

    @NotNull
    RatingPlayerRepository createRatingPlayerRepository();

    class SqliteConnectionFactory implements ConnectionFactory {

        private final Plugin plugin;
        private final String host;

        public SqliteConnectionFactory(@NotNull Plugin plugin, @NotNull ConfigurationSection section) throws ConfigLoadException {
            this.plugin = plugin;
            host = ConfigUtils.getString(section, "host");
        }

        @Override
        @NotNull
        public Connection openConnection() throws SQLException {
            return DriverManager.getConnection("jdbc:sqlite:" + new File(plugin.getDataFolder(), host).getPath());
        }

        @Override
        @NotNull
        public DeferredRewardRepository createDeferredRewardRepository() {
            return new SqliteDeferredRewardRepository(this);
        }

        @Override
        @NotNull
        public RatingPlayerRepository createRatingPlayerRepository() {
            return new SqliteRatingPlayerRepository(this);
        }
    }

    class MysqlConnectionFactory implements ConnectionFactory {

        private final String host;
        private final int port;
        private final String databaseName;
        private final String user;
        private final String password;

        public MysqlConnectionFactory(@NotNull ConfigurationSection section) throws ConfigLoadException {
            host = ConfigUtils.getString(section, "host");
            port = ConfigUtils.getPositiveInt(section, "port");
            databaseName = ConfigUtils.getString(section, "database_name");
            user = ConfigUtils.getString(section, "user");
            password = ConfigUtils.getString(section, "password");
        }

        @Override
        @NotNull
        public Connection openConnection() throws SQLException {
            return DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" +
                    databaseName + "?createDatabaseIfNotExist=true", user, password);
        }

        @Override
        @NotNull
        public DeferredRewardRepository createDeferredRewardRepository() {
            return new MySqlDeferredRewardRepository(this);
        }

        @Override
        @NotNull
        public RatingPlayerRepository createRatingPlayerRepository() {
            return new MySqlRatingPlayerRepository(this);
        }
    }
}
