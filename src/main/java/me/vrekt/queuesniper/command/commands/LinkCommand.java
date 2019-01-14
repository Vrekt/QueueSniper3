package me.vrekt.queuesniper.command.commands;

import io.github.robertograham.fortnite2.domain.enumeration.Platform;
import me.vrekt.queuesniper.async.Concurrent;
import me.vrekt.queuesniper.command.Command;
import me.vrekt.queuesniper.embed.EmbedUtility;
import me.vrekt.queuesniper.fortnite.AccountAPI;
import me.vrekt.queuesniper.guild.GuildConfiguration;
import me.vrekt.queuesniper.message.MessageAction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;

public class LinkCommand extends Command {

    public LinkCommand() {
        super("link", new String[]{}, false);
    }

    @Override
    public void execute(List<String> arguments, Member from, Message message, TextChannel channel, GuildConfiguration configuration) {
        if (arguments.size() < 2) {
            MessageAction.send(channel, from.getAsMention() + " the command goes like this: ``!link <platform> <name>``");
            return;
        }

        String platformParse = arguments.get(0);
        String name = arguments.get(1);

        Platform platform = parse(platformParse);
        if (platform == null) {
            MessageAction.send(channel, from.getAsMention() + " not a valid platform! Please type either ``PC, PS4, XBOX1``");
            return;
        }

        EmbedBuilder embed = EmbedUtility.getSnipeEmbed();
        embed.setFooter("Please wait while I query the API for your account.\nThis may take awhile depending on certain factors.\nIf it " +
                "has been more than 5 minutes the request is terminated.", null);
        MessageAction.send(channel, embed.build());

        Concurrent.runAsync(() -> AccountAPI.getAccountAndSetIfExists(from, channel, name, platform, configuration));
    }

    private Platform parse(String content) {
        switch (content.toLowerCase()) {
            case "xbox1":
            case "xboxone":
            case "xbox":
            case "xbone":
            case "xbx":
                return Platform.XB1;
            case "ps4":
            case "ps":
            case "psfour":
            case "four":
                return Platform.PS4;
            case "pc":
            case "kbm":
                return Platform.PC;
        }
        return null;
    }

}
