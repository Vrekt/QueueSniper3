package me.vrekt.queuesniper.guild.yaml;

import java.util.List;
import java.util.Map;

/**
 * A class used for saving a configuration.
 */
public class YamlConfiguration {

    public String guildId, announcementChannelId, commandChannelId, matchIdChannel, matchIdEmbedChannel, countdownChannel, prefix;
    public List<String> controllers, announcers;
    public Map<String, Map<String, String>> fortniteAccounts;

    public int countdownDelay, channelLock;

    public YamlConfiguration() {
    }

}
