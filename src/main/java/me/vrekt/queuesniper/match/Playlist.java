package me.vrekt.queuesniper.match;

public enum Playlist {

    SOLO("Solo"), DUO("Duo"), SQUAD("Squad"), ERR("Error");

    private final String prettyName;

    Playlist(String prettyName) {
        this.prettyName = prettyName;
    }

    public static Playlist of(String from) {
        switch (from.toLowerCase()) {
            case "solo":
            case "solos":
                return SOLO;
            case "duo":
            case "duos":
                return DUO;
            case "squad":
            case "quad":
            case "squads":
                return SQUAD;
        }
        return SQUAD;
    }

    public static Playlist ofExplicit(String from) {
        switch (from.toLowerCase()) {
            case "solo":
            case "solos":
                return SOLO;
            case "duo":
            case "duos":
                return DUO;
            case "squad":
            case "quad":
            case "squads":
                return SQUAD;
            case "team":
                return SQUAD;
        }
        return ERR;
    }

    public String getPrettyName() {
        return prettyName;
    }

}
