package ru.leonidm.millida.rating.repository;

import org.jetbrains.annotations.NotNull;
import ru.leonidm.millida.rating.config.api.ConnectionFactory;

import java.sql.ResultSet;

public abstract class MySqlAbstractRepository extends SqlAbstractRepository {

    protected MySqlAbstractRepository(@NotNull ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    protected void createIndexIfNotExists(@NotNull String index, @NotNull String table, @NotNull String column) {
        execute(
                "SELECT COUNT(1) FROM information_schema.statistics " +
                        "WHERE table_schema=DATABASE() AND table_name=? AND index_name=?",
                statement -> {
                    statement.setString(1, table);
                    statement.setString(2, index);
                    ResultSet resultSet = statement.executeQuery();

                    if (!resultSet.next()) {
                        return null;
                    }

                    int count = resultSet.getInt(1);
                    if (count == 0) {
                        statement.executeUpdate(String.format("CREATE INDEX %s ON %s(%s)", index, table, column));
                    }
                    return null;
                },
                "Could not create index '" + index + "' in '" + table + "'"
        );
    }
}
