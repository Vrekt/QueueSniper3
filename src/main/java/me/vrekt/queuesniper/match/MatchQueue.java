package me.vrekt.queuesniper.match;

import me.vrekt.queuesniper.async.Concurrent;
import me.vrekt.queuesniper.guild.GuildConfiguration;
import me.vrekt.queuesniper.match.voice.CountdownPlayer;
import me.vrekt.queuesniper.message.MessageAction;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used for queue'ing matches
 */
public class MatchQueue {

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final ConcurrentHashMap<GuildQueue, Long> QUEUE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Playlist, LinkedList<ASnipeMatch>> MATCHES = new ConcurrentHashMap<>();

    public static void start() {
        Concurrent.runAsyncTimer(MatchQueue::updateQueue, 1000);
    }

    /**
     * Queues a match
     *
     * @param configuration the guild
     * @param channel       the channel
     * @param type          the type of team/gamemode
     * @param queueDelay    the amount of time to wait in seconds before starting
     */
    public static void queue(GuildConfiguration configuration, TextChannel channel, Playlist type, int queueDelay, String messageId,
                             String announcersMessageId, boolean wasChannelProvided) {
        QUEUE.put(new GuildQueue(configuration, channel, type, queueDelay * 1000, messageId, announcersMessageId, wasChannelProvided),
                System.currentTimeMillis());
    }

    /**
     * Updates the list of queued matches
     */
    private static void updateQueue() {
        long now = System.currentTimeMillis();

        QUEUE.entrySet().removeIf(guild -> {
            GuildQueue queue = guild.getKey();
            if (queue.isReady(now)) {
                startMatch(queue);
                return true;
            }
            return false;
        });
    }

    /**
     * Starts the match
     *
     * @param guild the queue object
     */
    private static void startMatch(GuildQueue guild) {
        CountdownPlayer.countdown(guild.configuration.getGuild(), guild.configuration.getCountdownChannel(), "countdown.mp3");
        MATCHES.putIfAbsent(guild.type, new LinkedList<>());
        MATCHES.get(guild.type).add(new ASnipeMatch(guild.configuration, guild.type, guild.channel, guild.messageId,
                guild.announcersMessageId, guild.wasChannelProvided));
    }

    /**
     * Called when a match is finished collecting codes
     *
     * @param match the match
     */
    static void matchFinished(ASnipeMatch match) {
        MATCHES.get(match.getType()).remove(match);
    }

    /**
     * Cancels the most recent match
     *
     * @param type the type of team
     */
    public static void cancelMostRecent(Playlist type, TextChannel channel) {
        if (MATCHES.containsKey(type)) {
            LinkedList<ASnipeMatch> matches = MATCHES.get(type);
            if (matches.size() == 0) {
                MessageAction.send(channel, "No matches are in progress.");
                MATCHES.remove(type);
                return;
            }

            ASnipeMatch match = matches.get(0);
            match.cancel(channel.getJDA());
            matchFinished(match);
        } else {
            MessageAction.send(channel, "No matches are in progress.");
        }
    }

    /**
     * Holds data for a match that is about to start
     */
    public static class GuildQueue {

        private final GuildConfiguration configuration;
        private final long waitTime, queued;
        private final Playlist type;

        private final TextChannel channel;

        // for cancelling
        private final String messageId, announcersMessageId;
        private final boolean wasChannelProvided;

        GuildQueue(GuildConfiguration configuration, TextChannel channel, Playlist type, long waitTime, String messageId,
                   String announcersMessageId, boolean wasChannelProvided) {
            this.configuration = configuration;
            this.channel = channel;
            this.messageId = messageId;
            this.announcersMessageId = announcersMessageId;
            this.wasChannelProvided = wasChannelProvided;
            this.type = type;
            this.waitTime = waitTime;
            this.queued = System.currentTimeMillis();
        }

        GuildConfiguration getConfiguration() {
            return configuration;
        }

        public TextChannel getChannel() {
            return channel;
        }

        boolean isReady(long now) {
            return now - queued >= waitTime;
        }
    }

}
