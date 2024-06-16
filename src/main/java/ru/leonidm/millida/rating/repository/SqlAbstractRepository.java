package ru.leonidm.millida.rating.repository;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.millida.rating.config.v1.api.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Logger;

public abstract class SqlAbstractRepository {

    protected final Logger logger = Logger.getLogger("MillidaRating");

    private final ConnectionFactory connectionFactory;
    private Connection connection;

    protected SqlAbstractRepository(@NotNull ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @NotNull
    protected Optional<Connection> getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = connectionFactory.openConnection();
            }

            return Optional.of(connection);
        } catch (SQLException e) {
            logger.severe("Could not open new database connection");
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @NotNull
    protected Optional<PreparedStatement> getStatement(@NotNull String sql) {
        return getConnection().map(connection -> {
            try {
                return connection.prepareStatement(sql);
            } catch (SQLException e) {
                logger.severe("Could not open new database statement");
                e.printStackTrace();
                return null;
            }
        });
    }

    @NotNull
    protected <T> Optional<T> execute(@NotNull String sql, @NotNull SqlExecutable<T> sqlExecutable,
                                      @NotNull String exceptionMessage) {
        return getStatement(sql).map(statement -> {
            try {
                return sqlExecutable.run(statement);
            } catch (SQLException e) {
                logger.severe(exceptionMessage);
                e.printStackTrace();
                return null;
            } finally {
                try {
                    statement.close();
                } catch (SQLException ignored) {
                }
            }
        });
    }

    protected interface SqlExecutable<T> {

        @Nullable
        T run(@NotNull PreparedStatement statement) throws SQLException;

    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.severe("Could not close database connection");
        }
    }
}
