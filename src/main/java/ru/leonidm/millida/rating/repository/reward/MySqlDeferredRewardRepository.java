package ru.leonidm.millida.rating.repository.reward;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import ru.leonidm.millida.rating.api.entity.DeferredReward;
import ru.leonidm.millida.rating.api.repository.DeferredRewardRepository;
import ru.leonidm.millida.rating.config.v1.api.ConnectionFactory;
import ru.leonidm.millida.rating.repository.MySqlAbstractRepository;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MySqlDeferredRewardRepository extends MySqlAbstractRepository implements DeferredRewardRepository {

    public MySqlDeferredRewardRepository(@NotNull ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public void initialize() {
        execute(
                "CREATE TABLE IF NOT EXISTS millida_rating_deferred_rewards (" +
                        "id BIGINT PRIMARY KEY AUTO_INCREMENT, player_uuid VARCHAR(36), day INTEGER" +
                        ")",
                statement -> {
                    statement.execute();
                    return null;
                },
                "Could not create table 'deferred_rewards'"
        );

        createIndexIfNotExists("player_uuid_idx", "millida_rating_deferred_rewards", "player_uuid");
    }

    @Override
    @NotNull
    @Unmodifiable
    public List<DeferredReward> getDeferredRewards(@NotNull UUID uuid) {
        return execute(
                "SELECT id, day FROM millida_rating_deferred_rewards " +
                        "WHERE player_uuid = ?",
                statement -> {
                    statement.setString(1, uuid.toString());

                    List<DeferredReward> result = new ArrayList<>();
                    ResultSet resultSet = statement.executeQuery();
                    while (resultSet.next()) {
                        long id = resultSet.getLong("id");
                        int day = resultSet.getInt("day");

                        result.add(new DeferredReward(id, day));
                    }

                    return result;
                },
                "Could not select values from table 'millida_rating_deferred_rewards'"
        ).orElse(Collections.emptyList());
    }

    @Override
    public void addDeferredReward(@NotNull UUID uuid, int day) {
        execute(
                "INSERT INTO millida_rating_deferred_rewards (player_uuid, day) " +
                        "VALUES (?, ?)",
                statement -> {
                    statement.setString(1, uuid.toString());
                    statement.setInt(2, day);
                    statement.execute();
                    return null;
                },
                "Could not insert value into table 'millida_rating_deferred_rewards'"
        );
    }

    @Override
    public void deleteDeferredReward(@NotNull DeferredReward deferredReward) {
        execute(
                "DELETE FROM millida_rating_deferred_rewards " +
                        "WHERE id = ?",
                statement -> {
                    statement.setLong(1, deferredReward.getId());
                    statement.execute();
                    return null;
                },
                "Could not delete value from table 'millida_rating_deferred_rewards'"
        );
    }

    @Override
    public void clear() {
        execute(
                "DELETE FROM millida_rating_deferred_rewards",
                statement -> {
                    statement.execute();
                    return null;
                },
                "Could not delete values from table 'millida_rating_deferred_rewards'"
        );
    }
}
