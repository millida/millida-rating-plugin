package ru.leonidm.millida.rating.api.repository;

public interface StatisticRepository {

    void initialize();

    long getLastVote();

    void setLastVote(long lastVote);

    void close();

}
