package me.vrekt.queuesniper.command.commands;

import me.vrekt.queuesniper.command.Command;
import me.vrekt.queuesniper.embed.EmbedUtility;
import me.vrekt.queuesniper.guild.GuildConfiguration;
import me.vrekt.queuesniper.message.MessageAction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", new String[]{"halp", "how", "tutorial"}, true);
    }

    @Override
    public void execute(List<String> arguments, Member from, Message message, TextChannel channel, GuildConfiguration configuration) {
        EmbedBuilder embed = EmbedUtility.getSnipeEmbed();

        // TODO: Commands enum so this isn't 'hard coded'
        String prefix = configuration.getPrefix();
        embed.setTitle("Found a problem or need further help? Contact: vrekt#4387 on discord");

        String commands = "**" + prefix + "start <gamemode> (optional) #channel**" +
                "\n*This command starts a snipe match, if you specify a channel then all embeds will go there*\n" +
                "*Example: " + prefix + "start duos*\n\n**" + prefix + "cancel <gamemode>**\n*" +
                "This command cancels a match that is collecting codes.*\n\n**" + prefix + "lock**\n" +
                "*This command locks the channel you type it in.*\n\n**" + prefix + "unlock**\n" +
                "*This command unlocks the channel you type it in.*\n\n**" + prefix + "link <platform> <name>**\n*" +
                "This command allows players to link their Fortnite accounts, this is not verified.*\n" +
                "*Example: " + prefix + "link pc vrekt_*\n\n**" + prefix + "unlink**\n*This command allows players to unlink their " +
                "Fortnite accounts.*\n" +
                "*If you are an administrator you can do " + prefix + " unlink @player to unlink their account*\n\n" +
                "**" + prefix + "stats**\n*Allows players to view their stats.*\n\n**" + prefix + "stats @player**\n*" +
                "Allows players to view another players stats.*\n\n**" + prefix + "config**\n*Allows you to change certain config options" +
                ".*";

        embed.addField("\u200b", commands, false);
        MessageAction.send(channel, embed.build());
    }
}
