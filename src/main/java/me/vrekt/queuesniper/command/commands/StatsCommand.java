package me.vrekt.queuesniper.command.commands;

import me.vrekt.queuesniper.async.Concurrent;
import me.vrekt.queuesniper.command.Command;
import me.vrekt.queuesniper.fortnite.AccountAPI;
import me.vrekt.queuesniper.guild.GuildConfiguration;
import me.vrekt.queuesniper.message.MessageAction;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class StatsCommand extends Command {

    public StatsCommand() {
        super("stats", new String[]{"score", "statistics", "ftn", "fn"}, false);
    }

    @Override
    public void execute(List<String> arguments, Member from, Message message, TextChannel channel, GuildConfiguration configuration) {

        if (arguments.isEmpty()) {
            if (!configuration.hasLinked(from.getUser().getId())) {
                MessageAction.send(channel, from.getAsMention() + " you have not linked your account! Do !link <platform> <name>");
                return;
            }

            MessageAction.send(channel, from.getAsMention() + " please wait while I grab your stats.");
            Concurrent.runAsync(() -> AccountAPI.showStats(from, channel, configuration.getAccount(from.getUser().getId()), configuration));
        }
    }
}
