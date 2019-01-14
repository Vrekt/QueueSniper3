package me.vrekt.queuesniper.command.commands;

import me.vrekt.queuesniper.command.Command;
import me.vrekt.queuesniper.embed.EmbedUtility;
import me.vrekt.queuesniper.guild.GuildConfiguration;
import me.vrekt.queuesniper.match.MatchQueue;
import me.vrekt.queuesniper.match.Playlist;
import me.vrekt.queuesniper.message.MessageAction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.util.List;

public class StartCommand extends Command {

    public StartCommand() {
        super("start", new String[]{}, true);
    }

    @Override
    public void execute(List<String> arguments, Member from, Message message, TextChannel channel, GuildConfiguration configuration) {
        if (arguments.size() == 0) {
            MessageAction.send(channel, from.getAsMention() + " please type a gamemode to start. Valid options are: solos, duos, squads\n" +
                    "You can also select a channel to announce to, like this: ``!start duos #channel``");
            return;
        }

        Playlist type = Playlist.ofExplicit(arguments.get(0));
        if (type == Playlist.ERR) {
            MessageAction.send(channel, from.getAsMention() + " not a valid gamemode!");
            return;
        }

        // \n\u200b is for spacing
        EmbedBuilder embed = EmbedUtility.getSnipeEmbed();
        embed.setAuthor("SNIPE MATCH STARTING, PAY ATTENTION!", null, null);

        embed.addField("Match starting soon:", "- A match is starting in " + configuration.getCountdownDelay() + " seconds, get " +
                "ready!\n\u200b", false);
        embed.addField("Gamemode:", type.name() + "S\n\u200b", false);
        embed.addField("Match instructions: ", "1. Join the voice channel: " + configuration.getCountdownChannel().getName() + ".\n2. " +
                "Click play in-game when you hear 'GO'.\n\n*Make sure you type your codes!*", false);

        embed.setFooter("Match hosted by: " + from.getEffectiveName() + "#" + from.getUser().getDiscriminator(),
                from.getUser().getAvatarUrl());
        embed.setTimestamp(message.getTimeCreated());

        final TextChannel sendTo = (message.getMentionedChannels().isEmpty() ? configuration.getAnnouncementChannel() :
                message.getMentionedChannels().get(0));

        try {
            sendTo.sendMessage(configuration.buildAnnouncers()).queue(msg ->
                    sendTo.sendMessage(embed.build()).queue(msg1 ->
                            MatchQueue.queue(configuration, sendTo, type, configuration.getCountdownDelay(),
                                    msg1.getId(), msg.getId(), !message.getMentionedChannels().isEmpty())));
        } catch (ErrorResponseException | InsufficientPermissionException exception) {
            //
        }
    }
}
