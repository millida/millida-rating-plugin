package ru.leonidm.millida.rating.repository.player;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.leonidm.millida.rating.api.entity.RatingPlayer;
import ru.leonidm.millida.rating.api.repository.RatingPlayerRepository;
import ru.leonidm.millida.rating.config.api.ConnectionFactory;
import ru.leonidm.millida.rating.repository.SqlAbstractRepository;

import java.sql.ResultSet;
import java.util.UUID;

public class SqliteRatingPlayerRepository extends SqlAbstractRepository implements RatingPlayerRepository {

    public SqliteRatingPlayerRepository(@NotNull ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public void initialize() {
        execute(
                "CREATE TABLE IF NOT EXISTS millida_rating_players (" +
                        "uuid VARCHAR(36), streak INTEGER, last_vote BIGINT" +
                        ")",
                statement -> {
                    statement.execute();
                    return null;
                },
                "Could not create table 'millida_rating_players'"
        );

        execute(
                "CREATE INDEX IF NOT EXISTS uuid_idx ON millida_rating_players(uuid)",
                statement -> {
                    statement.execute();
                    return null;
                },
                "Could not create index 'uuid_idx' in table 'millida_rating_players'"
        );
    }

    @Override
    @Nullable
    public RatingPlayer findRatingPlayer(@NotNull UUID uuid) {
        return execute(
                "SELECT streak, last_vote FROM millida_rating_players " +
                        "WHERE uuid = ?",
                statement -> {
                    statement.setString(1, uuid.toString());
                    ResultSet resultSet = statement.executeQuery();

                    if (!resultSet.next()) {
                        return null;
                    }

                    int streak = resultSet.getInt("streak");
                    long lastVoteTimestamp = resultSet.getLong("last_vote");
                    return new RatingPlayer(uuid, streak, lastVoteTimestamp);
                },
                "Could not select value from table 'millida_rating_players'"
        ).orElse(null);
    }

    @Override
    @Nullable
    public RatingPlayer createRatingPlayer(@NotNull UUID uuid) {
        return execute(
                "INSERT INTO millida_rating_players (uuid, streak, last_vote) " +
                        "VALUES (?, ?, ?) " +
                        "RETURNING streak, last_vote",
                statement -> {
                    statement.setString(1, uuid.toString());
                    statement.setLong(2, 0);
                    statement.setLong(3, 0);
                    ResultSet resultSet = statement.executeQuery();

                    if (!resultSet.next()) {
                        return null;
                    }

                    int streak = resultSet.getInt("streak");
                    long lastVoteTimestamp = resultSet.getLong("last_vote");
                    return new RatingPlayer(uuid, streak, lastVoteTimestamp);
                },
                "Could not insert value into table 'millida_rating_players'"
        ).orElse(null);
    }

    @Override
    public void saveRatingPlayer(@NotNull RatingPlayer ratingPlayer) {
        execute(
                "UPDATE millida_rating_players " +
                        "SET streak = ?, last_vote = ? " +
                        "WHERE uuid = ?",
                statement -> {
                    statement.setLong(1, ratingPlayer.getStreak());
                    statement.setLong(2, ratingPlayer.getLastVoteTimestamp());
                    statement.setString(3, ratingPlayer.getUuid().toString());
                    statement.execute();
                    return null;
                },
                "Could not update value of table 'millida_rating_players'"
        );
    }

    @Override
    public void deleteRatingPlayer(@NotNull UUID uuid) {
        execute(
                "DELETE FROM millida_rating_players " +
                        "WHERE uuid = ?",
                statement -> {
                    statement.setString(1, uuid.toString());
                    statement.execute();
                    return null;
                },
                "Could not delete value from table 'millida_rating_players'"
        );
    }

    @Override
    public void clear() {
        execute(
                "DELETE FROM millida_rating_players",
                statement -> {
                    statement.execute();
                    return null;
                },
                "Could not delete values from table 'millida_rating_players'"
        );
    }
}
