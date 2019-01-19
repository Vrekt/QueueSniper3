package me.vrekt.queuesniper.fortnite.match;

import io.github.robertograham.fortnite2.domain.Account;
import io.github.robertograham.fortnite2.domain.Statistic;
import net.dv8tion.jda.api.entities.Member;

public class Player {

    private final Member member;
    private final Account account;
    private final String server;

    private final Statistic past;

    private boolean isAlive;
    private int playerCount;

    Player(Member member, Account account, Statistic current, String server) {
        this.member = member;
        this.account = account;
        this.server = server;
        this.past = current;

        isAlive = true;
    }

    public Member getMember() {
        return member;
    }

    public Account getAccount() {
        return account;
    }

    Statistic getPast() {
        return past;
    }

    String getServer() {
        return server;
    }

    boolean isAlive() {
        return isAlive;
    }

    void setAlive(boolean alive) {
        isAlive = alive;
    }

    int getPlayerCount() {
        return playerCount;
    }

    void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    Placement getPlacement(Statistic present) {
        boolean won = present.wins() - past.wins() != 0;
        boolean placedTop25 = present.timesPlacedTop25() - past.timesPlacedTop25() != 0;
        boolean placedTop12 = present.timesPlacedTop12() - past.timesPlacedTop12() != 0;
        boolean placedTop10 = present.timesPlacedTop10() - past.timesPlacedTop10() != 0;
        boolean placedTop6 = present.timesPlacedTop6() - past.timesPlacedTop6() != 0;
        boolean placedTop5 = present.timesPlacedTop5() - past.timesPlacedTop5() != 0;
        boolean placedTop3 = present.timesPlacedTop3() - past.timesPlacedTop3() != 0;

        if (won) {
            return Placement.WIN;
        } else if (placedTop3) {
            return Placement.TOP_3;
        } else if (placedTop5) {
            return Placement.TOP_5;
        } else if (placedTop6) {
            return Placement.TOP_6;
        } else if (placedTop10) {
            return Placement.TOP_10;
        } else if (placedTop12) {
            return Placement.TOP_12;
        } else if (placedTop25) {
            return Placement.TOP_25;
        }
        return null;
    }

    enum Placement {
        WIN("1st"), TOP_3("2/3"), TOP_5("5th"), TOP_6("6th"), TOP_10("10th"), TOP_12("12th"), TOP_25("25th");

        private final String name;

        Placement(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}
