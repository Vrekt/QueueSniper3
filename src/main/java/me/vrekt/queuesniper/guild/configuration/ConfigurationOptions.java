package me.vrekt.queuesniper.guild.configuration;

public enum ConfigurationOptions {

    CONTROLLERS(0, "Administrator/Control roles", "List of roles that have permission to control the bot.",
            "Mention or type the name of the roles that will be allowed to control the bot.\n(You can mention multiple roles)",
            "@Host @Admin", false, true),
    ANNOUNCERS(1, "Announcement roles", "List of roles that will be mentioned when any announcement occurs.",
            "Mention or type the name of the roles that will be mentioned when any announcement occurs.\n(You can mention multiple roles)",
            "@Player @Pro", false, true),
    ANNOUNCEMENT_CHANNEL(2, "Announcement text channel", "The channel used for announcements.",
            "Mention or type the name of the text channel that will be used for announcements.",
            "#announcements", true, false),
    COMMAND_CHANNEL(3, "Command channel", "The channel members will use for commands.",
            "Mention or type the name of the text channel that will be used for commands.\nNOTE: This channel will be the only channel " +
                    "where members can use team commands or other user commands. Administrator commands can be executed anywhere.",
            "#commands", true, false),
    MATCH_ID_CHANNEL(4, "Match ID channel", "The channel members will use to post their match IDs.",
            "Mention or type the name of the text channel that will be used by players to post their match IDs.\nThis channel will be " +
                    "monitored by the bot so it can collect and show codes.\nNOTE: This channel is locked after match IDs are collected.",
            "#codes", true, false),
    MATCH_ID_EMBED_CHANNEL(5, "Match ID list channel", "The channel that will be used to post the list of matches.",
            "Mention or type the name of the text channel that will be used to show all match IDs.\nThis channel can be the same as the " +
                    "regular match ID channel.\nNOTE: It may not be desirable to make both channels if you have alot of members playing " +
                    "since deleting messages is rate-limited by discord. As a result of this the embed may scroll out of view.",
            "#codes", true, false),
    COUNTDOWN_CHANNEL(6, "Countdown channel", "The voice channel used for counting down when a match is starting.",
            "Enter the name of the voice channel that will be used for countdown.",
            "Countdown", true),
    COUNTDOWN_DELAY(7, "Countdown delay", "How many seconds to wait before counting down in the voice channel",
            "Enter the amount of delay to wait before counting down in the voice channel.\nNOTE: This value is in seconds.\nDefault value" +
                    " is 3",
            "3", "8"),
    CHANNEL_LOCK(8, "Channel lock", "How long should members be allowed to enter their match ID before the channel is locked.",
            "Enter the amount of minutes players are allowed to enter their match IDs in the match ID channel.\nNOTE: This value is in " +
                    "seconds\nRecommended value is 120",
            "120", "120"),
    PREFIX(9, "Command prefix", "The prefix used for commands", "What command prefix should be used?\nDefault is .", "!");

    private final String name, description, setupString, defaultValue, example;
    private final int index;

    private boolean textChannel, role, voiceChannel;

    ConfigurationOptions(int index, String name, String description, String setupString, String defaultValue, String example) {
        this.index = index;
        this.name = name;
        this.description = description;
        this.setupString = setupString;
        this.defaultValue = defaultValue;
        this.example = example;
    }

    ConfigurationOptions(int index, String name, String description, String setupString, String example) {
        this(index, name, description, setupString, null, example);
    }

    ConfigurationOptions(int index, String name, String description, String setupString, String example, boolean isTextChannel, boolean isRole) {
        this(index, name, description, setupString, null, example);
        this.textChannel = isTextChannel;
        this.role = isRole;
    }

    ConfigurationOptions(int index, String name, String description, String setupString, String example, boolean isVoiceChannel) {
        this(index, name, description, setupString, null, example);
        this.voiceChannel = isVoiceChannel;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getSetupString() {
        return setupString;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getExample() {
        return example;
    }

    public boolean isTextChannel() {
        return textChannel;
    }

    public boolean isVoiceChannel() {
        return voiceChannel;
    }

    public boolean isRole() {
        return role;
    }

    public boolean hasNext() {
        return index + 1 < values().length;
    }

    public ConfigurationOptions next() {
        return values()[index + 1];
    }

}
