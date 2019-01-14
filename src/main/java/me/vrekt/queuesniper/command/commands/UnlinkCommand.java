package me.vrekt.queuesniper.command.commands;

import me.vrekt.queuesniper.command.Command;
import me.vrekt.queuesniper.guild.GuildConfiguration;
import me.vrekt.queuesniper.message.MessageAction;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class UnlinkCommand extends Command {

    public UnlinkCommand() {
        super("unlink", new String[]{}, false);
    }

    @Override
    public void execute(List<String> arguments, Member from, Message message, TextChannel channel, GuildConfiguration configuration) {

        boolean isAdministrator = from.hasPermission(Permission.ADMINISTRATOR) || configuration.isController(from);
        if (isAdministrator && !message.getMentionedMembers().isEmpty()) {
            MessageAction.send(channel, from.getAsMention() + " unlinking their account if they have linked one.");
            configuration.removeAccount(message.getMentionedMembers().get(0).getUser().getId());
            return;
        }

        if (!configuration.hasLinked(from.getUser().getId())) {
            MessageAction.send(channel, from.getAsMention() + " you have not linked your fortnite account!");
            return;
        }

        configuration.removeAccount(from.getUser().getId());
        MessageAction.send(channel, from.getAsMention() + " successfully unlinked your account.");
    }
}
