package me.vrekt.queuesniper.command.commands;

import me.vrekt.queuesniper.command.Command;
import me.vrekt.queuesniper.guild.GuildConfiguration;
import me.vrekt.queuesniper.match.MatchQueue;
import me.vrekt.queuesniper.match.Playlist;
import me.vrekt.queuesniper.message.MessageAction;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class CancelCommand extends Command {

    public CancelCommand() {
        super("cancel", new String[]{}, true);
    }

    @Override
    public void execute(List<String> arguments, Member from, Message message, TextChannel channel, GuildConfiguration configuration) {

        if (arguments.size() < 1) {
            showUsage(channel);
            return;
        }

        Playlist type = Playlist.ofExplicit(arguments.get(0));
        if (type == Playlist.ERR) {
            showUsage(channel);
            return;
        }

        MatchQueue.cancelMostRecent(type, channel);
    }

    private void showUsage(TextChannel channel) {
        MessageAction.send(channel, "Usage: ``!cancel <gamemode>``\n``!cancel duos``\nThis command cancels a match that is collecting " +
                "codes.");
    }

}
