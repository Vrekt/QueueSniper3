package me.vrekt.queuesniper.fortnite.match;

import net.dv8tion.jda.api.entities.Member;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public interface StatisticCapture {
    long matchTime = Duration.of(30, ChronoUnit.MINUTES).toMillis();

    void finalizePlayersInMatch(Map<Member, String> players);

    void updateMatch();

    void finalizeMatch();

}
