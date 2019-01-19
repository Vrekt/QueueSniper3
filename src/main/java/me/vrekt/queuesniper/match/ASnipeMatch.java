package me.vrekt.queuesniper.match;

import me.vrekt.queuesniper.async.Concurrent;
import me.vrekt.queuesniper.guild.GuildConfiguration;
import me.vrekt.queuesniper.message.MessageAction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.awt.Color;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ASnipeMatch {

    private final GuildConfiguration configuration;
    private final Playlist type;

    private final Map<String, LinkedList<String>> servers = new LinkedHashMap<>();

    private final List<String> players = new ArrayList<>();

    private final EmbedBuilder matchIdEmbed = new EmbedBuilder();
    private final TextChannel matchIdEmbedChannel, matchIdChannel, channel;

    private final boolean wasChannelProvided;

    private final String startingMessageId, taskId = UUID.randomUUID().toString();

    private String embedMessageId, embedInfoMessageId, announcersMessageId;

    ASnipeMatch(GuildConfiguration configuration, Playlist type, TextChannel channel, String startingMessageId,
                String announcersMessageId, boolean wasChannelProvided) {

        this.configuration = configuration;
        this.type = type;

        this.channel = channel;
        this.startingMessageId = startingMessageId;
        this.announcersMessageId = announcersMessageId;
        this.wasChannelProvided = wasChannelProvided;

        matchIdEmbedChannel = configuration.getMatchIdEmbedChannel();
        matchIdChannel = configuration.getMatchIdChannel();

        EmbedBuilder embed = new EmbedBuilder().setColor(new Color(114, 109, 168));
        embed.addField("Waiting for match IDs...",
                "*Please post the last 3 characters of your match ID once you are in the game! " +
                        "Instructions:*\n\n" +
                        "This can be found in the top left of your screen.\n" +
                        "Match IDs should go in the channel: " + configuration.getMatchIdChannel().getAsMention() + "",
                false);
        embed.setImage("https://i.imgur.com/z4kiUNS.png");
        embed.setFooter("Players participating: " + configuration.getCountdownChannel().getMembers().size(), null);

        TextChannel announcements = wasChannelProvided ? channel : configuration.getAnnouncementChannel();

        try {
            announcements.sendMessage(embed.build()).queue(message -> embedInfoMessageId = message.getId());
        } catch (ErrorResponseException | InsufficientPermissionException exception) {
            lock();
            return;
        }

        matchIdEmbed.setColor(new Color(160, 210, 219)).setDescription("Waiting for IDs...");
        try {
            if (wasChannelProvided) {
                channel.sendMessage(matchIdEmbed.build()).queue(message -> embedMessageId = message.getId());
            } else {
                matchIdEmbedChannel.sendMessage(matchIdEmbed.build()).queue(message -> embedMessageId = message.getId());
            }
        } catch (ErrorResponseException | InsufficientPermissionException exception) {
            lock();
            return;
        }

        unlock();

        channel.getJDA().addEventListener(this);
        Concurrent.runAsyncLater(() -> finish(channel.getJDA()), Duration.of(configuration.getChannelLock(), ChronoUnit.SECONDS).toMillis(), taskId);
    }

    @SubscribeEvent
    public void onGuildMessage(GuildMessageReceivedEvent event) {
        if (event.isWebhookMessage()) return;

        Guild guild = event.getGuild();
        TextChannel channel = event.getChannel();
        if (!guild.getId().equals(configuration.getGuild().getId())) return;
        if (!channel.getId().equals(wasChannelProvided ? this.channel.getId() : matchIdChannel.getId())) return;

        Member from = event.getMember();
        if (configuration.isSelf(from)) return;

        Message message = event.getMessage();
        handleCollection(message, channel);
    }

    /**
     * Handles collecting codes.
     *
     * @param message the message
     * @param channel the channel
     */
    private void handleCollection(Message message, TextChannel channel) {
        Member member = message.getMember();
        String player = member.getAsMention();
        String serverId = message.getContentDisplay().toLowerCase();

        if (serverId.length() == 3) {
            if (!servers.containsKey(serverId) && !players.contains(player)) {
                LinkedList<String> serverPlayers = new LinkedList<>();
                serverPlayers.add(member.getUser().getId());

                servers.put(serverId, serverPlayers);
                players.add(player);
                editEmbed();
            } else if (servers.containsKey(serverId) && !players.contains(player)) {
                servers.get(serverId).add(member.getUser().getId());
                players.add(player);
                editEmbed();
            }

        }

        // don't delete commands
        boolean isAdministrator = member.hasPermission(Permission.ADMINISTRATOR) || configuration.isController(member);
        if (isAdministrator) {
            if (serverId.startsWith(configuration.getPrefix())) {
                return;
            }
        }
        MessageAction.delete(channel, message.getId());
    }

    /**
     * Sorts the embed and then edits it
     */
    private void editEmbed() {
        AtomicInteger fancy = new AtomicInteger(1);
        StringBuilder singleBuilder = new StringBuilder();
        matchIdEmbed.clearFields().setDescription("").setTitle("[PLAYERS AND SERVERS]");

        // TODO: Better sorting?

        Map<String, List<String>> sorted =
                servers.entrySet().stream().sorted(Comparator.comparingInt(o -> o.getValue().size())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k, v) -> k,
                        LinkedHashMap::new));

        List<Map.Entry<String, List<String>>> list = new ArrayList<>(sorted.entrySet());
        for (int i = list.size() - 1; i >= 0; i--) {
            Map.Entry<String, List<String>> entry = list.get(i);
            String serverId = entry.getKey();
            List<String> playersEdit = entry.getValue();
            List<String> players = new ArrayList<>();

            playersEdit.forEach(id -> {
                String accountName = configuration.hasLinked(id) ? configuration.getAccountName(id) : null;
                String name = configuration.getGuild().getMemberById(id).getAsMention();

                if (accountName != null) {
                    name = name.concat(" (" + accountName + ")");
                }
                players.add(name);
            });

            fancy.incrementAndGet();

            if (players.size() >= 28) {
                int skipAmount = 16;
                LinkedList<String> skipped = skip(skipAmount, players);
                if (skipped.size() >= 28) {
                    skipAmount += 4;
                    skipped = skip(skipAmount, players);
                }
                int amountSkipped = players.size() - skipped.size();
                String field = String.join("\n", skipped) + "\n*and " + amountSkipped + " more players...*";
                matchIdEmbed.addField("ID: " + serverId + " (" + players.size() + " players)", field, true);
            } else if (players.size() > 1) {
                String field = String.join("\n", players);
                matchIdEmbed.addField("ID: " + serverId + " (" + players.size() + " players)", field, true);
            }

            if (fancy.get() % 2 == 0) {
                matchIdEmbed.addBlankField(true);
            }

            if (players.size() == 1) {
                String player = players.get(0);
                String append = player + " (" + serverId + "), ";
                singleBuilder.append(append);
            }
        }

        if (singleBuilder.length() != 0) {
            matchIdEmbed.addField("Single lobbies: ", singleBuilder.toString(), false);
        }

        MessageAction.edit(wasChannelProvided ? channel : matchIdEmbedChannel, embedMessageId, matchIdEmbed.build());
    }

    private LinkedList<String> skip(int amount, List<String> list) {
        return list.stream().skip(amount).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Called when the bot is done collecting codes
     *
     * @param jda jda
     */
    private void finish(JDA jda) {
        MatchQueue.matchFinished(this);
        jda.removeEventListener(this);
        lock();

        matchIdEmbed.setFooter("Match started: (" + players.size() + " players) (" + servers.keySet().size() + " servers)", null);
        MessageAction.send(matchIdChannel, "*Chat locked...* Match started, good luck and have fun!");
        MessageAction.edit(matchIdEmbedChannel, embedMessageId, matchIdEmbed.build());

        servers.clear();
        players.clear();
    }

    Playlist getType() {
        return type;
    }

    /**
     * Deletes the embed and locks the channel again
     *
     * @param jda jda
     */
    void cancel(JDA jda) {
        Concurrent.cancelTask(taskId);
        MatchQueue.matchFinished(this);
        jda.removeEventListener(this);

        lock();

        if (wasChannelProvided) {
            MessageAction.delete(channel, announcersMessageId);
            MessageAction.delete(channel, startingMessageId);
            MessageAction.delete(channel, embedInfoMessageId);
            MessageAction.delete(channel, embedMessageId);
        } else {
            MessageAction.delete(configuration.getAnnouncementChannel(), announcersMessageId);
            MessageAction.delete(configuration.getAnnouncementChannel(), startingMessageId);
            MessageAction.delete(configuration.getAnnouncementChannel(), embedInfoMessageId);
            MessageAction.delete(matchIdEmbedChannel, embedMessageId);
        }

        servers.clear();
        players.clear();
    }

    /**
     * Locks the appropriate channels
     */
    private void lock() {
        if (wasChannelProvided) {
            try {
                channel.putPermissionOverride(configuration.getGuild().getPublicRole()).setDeny(Permission.MESSAGE_WRITE).queue();
                channel.putPermissionOverride(configuration.getGuild().getPublicRole()).setDeny(Permission.MESSAGE_ADD_REACTION).queue();
                for (Role announcer : configuration.getAnnouncers()) {
                    channel.putPermissionOverride(announcer).setDeny(Permission.MESSAGE_WRITE).queue();
                    channel.putPermissionOverride(announcer).setDeny(Permission.MESSAGE_ADD_REACTION).queue();
                }
            } catch (ErrorResponseException | InsufficientPermissionException exception) {
                //
            }
        } else {
            try {
                matchIdChannel.putPermissionOverride(configuration.getGuild().getPublicRole()).setDeny(Permission.MESSAGE_WRITE).queue();
                matchIdChannel.putPermissionOverride(configuration.getGuild().getPublicRole()).setDeny(Permission.MESSAGE_ADD_REACTION).queue();
                for (Role announcer : configuration.getAnnouncers()) {
                    matchIdChannel.putPermissionOverride(announcer).setDeny(Permission.MESSAGE_WRITE).queue();
                    matchIdChannel.putPermissionOverride(announcer).setDeny(Permission.MESSAGE_ADD_REACTION).queue();
                }
            } catch (ErrorResponseException | InsufficientPermissionException exception) {
                //
            }
        }
    }

    /**
     * Unlocks the appropriate channel
     */
    private void unlock() {
        if (wasChannelProvided) {
            try {
                channel.putPermissionOverride(configuration.getGuild().getPublicRole()).setAllow(Permission.MESSAGE_WRITE).queue();
                for (Role announcer : configuration.getAnnouncers()) {
                    channel.putPermissionOverride(announcer).setAllow(Permission.MESSAGE_WRITE).queue();
                }
            } catch (ErrorResponseException | InsufficientPermissionException exception) {
                //
            }
        } else {
            try {
                matchIdChannel.putPermissionOverride(configuration.getGuild().getPublicRole()).setAllow(Permission.MESSAGE_WRITE).queue();
                for (Role announcer : configuration.getAnnouncers()) {
                    matchIdChannel.putPermissionOverride(announcer).setAllow(Permission.MESSAGE_WRITE).queue();
                }
            } catch (ErrorResponseException | InsufficientPermissionException exception) {
                //
            }
        }
    }

}
