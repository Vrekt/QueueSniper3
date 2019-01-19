package me.vrekt.queuesniper.fortnite.match;

import io.github.robertograham.fortnite2.domain.Account;
import io.github.robertograham.fortnite2.domain.Statistic;
import me.vrekt.queuesniper.async.Concurrent;
import me.vrekt.queuesniper.embed.EmbedUtility;
import me.vrekt.queuesniper.fortnite.AccountAPI;
import me.vrekt.queuesniper.guild.GuildConfiguration;
import me.vrekt.queuesniper.match.Playlist;
import me.vrekt.queuesniper.message.MessageAction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class MatchStatisticCapture implements StatisticCapture {

    private final EmbedBuilder playersAliveEmbed = EmbedUtility.getSnipeEmbed();
    private final List<Player> players = new LinkedList<>();

    private final Playlist playlist;
    private final GuildConfiguration configuration;

    private final String taskId;
    private final TextChannel channel;

    private long queued;
    private String messageId;

    public MatchStatisticCapture(Playlist playlist, GuildConfiguration configuration, TextChannel channel) {
        this.playlist = playlist;
        this.configuration = configuration;
        this.channel = channel;

        this.taskId = UUID.randomUUID().toString();

        playersAliveEmbed.setDescription("Waiting 1 minute before updating statistics...");
    }

    @Override
    public void finalizePlayersInMatch(Map<Member, String> players) {
        final Map<Member, String> finalPlayers = new HashMap<>(players);

        try {
            channel.sendMessage(playersAliveEmbed.build()).queue(message -> messageId = message.getId());
        } catch (ErrorResponseException | InsufficientPermissionException exception) {
            //
        }

        Concurrent.runAsync(() -> finalPlayers.forEach(((member, server) -> {
            Account account = configuration.getAccount(member.getUser().getId());
            Statistic current = AccountAPI.getStatistic(account, member, playlist, configuration);
            if (current == null) return;
            this.players.add(new Player(member, account, current, server));
        })));

        Concurrent.runAsyncTimer(this::updateMatch, taskId, 80000);

        queued = System.currentTimeMillis();
        setPlayersForLobby();
    }

    @Override
    public void updateMatch() {

        try {
            playersAliveEmbed.setDescription("");
            playersAliveEmbed.setAuthor("Players still alive: ", null, configuration.getSelf().getUser().getAvatarUrl());

            long elapsed = System.currentTimeMillis() - queued;
            if (elapsed >= matchTime) {
                finalizeMatch();
                return;
            }

            playersAliveEmbed.clearFields();

            players.forEach(player -> {
                playersAliveEmbed.addField("(" + player.getAccount().displayName() + ")", "Status: " + (player.isAlive() ? "Alive" :
                        "Eliminated") + "\nServer: " + player.getServer() + "\nUser: " + player.getMember().getAsMention(), false);

                if (player.isAlive()) {
                    Statistic now = AccountAPI.getStatistic(player.getAccount(), player.getMember(), playlist, configuration);

                    if (now != null && !(now.equals(player.getPast()))) {
                        // statistic updated, check what happened
                        player.setAlive(false);

                        Player.Placement placement = player.getPlacement(now);
                        long kills = now.kills() - player.getPast().kills();

                        EmbedBuilder embed = EmbedUtility.getSnipeEmbed();
                        embed.setDescription("Match finished! Here is a summary:\n" +
                                "Total kills: " + kills + (placement == null ? "" : "\nPlacement: " + placement.getName()) + "\nServer" +
                                ": " + player.getServer() + "\nA total of " + player.getPlayerCount() +
                                " " +
                                "different scrim players were in your lobby!");
                        player.getMember().getUser().openPrivateChannel().queue(channel -> channel.sendMessage(embed.build()).queue());
                    }
                }
            });

            boolean anyAlive = this.players.stream().anyMatch(Player::isAlive);
            if (!anyAlive) {
                MessageAction.send(channel, configuration.buildControllers() + " all matches have finished.");
                finalizeMatch();
                return;
            }

            MessageAction.edit(channel, messageId, playersAliveEmbed.build());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void finalizeMatch() {
        players.clear();

        MessageAction.delete(channel, messageId);
        Concurrent.cancelTask(taskId);
    }

    private void setPlayersForLobby() {
        players.forEach(player -> {
            AtomicInteger count = new AtomicInteger();
            players.stream().filter(p -> p.getServer().equals(player.getServer())).forEach(p -> count.incrementAndGet());

            player.setPlayerCount(count.get());
        });
    }
}
