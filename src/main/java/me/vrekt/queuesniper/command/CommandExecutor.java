package me.vrekt.queuesniper.command;

import me.vrekt.queuesniper.command.commands.CancelCommand;
import me.vrekt.queuesniper.command.commands.ConfigurationCommand;
import me.vrekt.queuesniper.command.commands.HelpCommand;
import me.vrekt.queuesniper.command.commands.LinkCommand;
import me.vrekt.queuesniper.command.commands.LockCommand;
import me.vrekt.queuesniper.command.commands.SetupCommand;
import me.vrekt.queuesniper.command.commands.StartCommand;
import me.vrekt.queuesniper.command.commands.StatsCommand;
import me.vrekt.queuesniper.command.commands.UnlinkCommand;
import me.vrekt.queuesniper.command.commands.UnlockCommand;
import me.vrekt.queuesniper.embed.EmbedUtility;
import me.vrekt.queuesniper.guild.GuildConfiguration;
import me.vrekt.queuesniper.guild.GuildConfigurationBuilder;
import me.vrekt.queuesniper.guild.GuildConfigurationImpl;
import me.vrekt.queuesniper.message.MessageAction;
import me.vrekt.queuesniper.utility.PermissionCheck;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class CommandExecutor {

    private final List<Command> commands = new ArrayList<>();

    public CommandExecutor(JDA jda, boolean buildSuccessful) {
        commands.add(new SetupCommand(jda));
        commands.add(new ConfigurationCommand());
        commands.add(new StartCommand());
        commands.add(new LockCommand());
        commands.add(new UnlockCommand());
        commands.add(new CancelCommand());
        if (buildSuccessful) {
            commands.add(new LinkCommand());
            commands.add(new UnlinkCommand());
            commands.add(new StatsCommand());
        }
        commands.add(new HelpCommand());
        jda.addEventListener(this);
    }

    /**
     * Attempt to execute a command
     *
     * @param message       the message
     * @param from          who it was sent from
     * @param channel       the channel it was sent in
     * @param configuration the guild it was sent from
     */
    private void execute(Message message, Member from, TextChannel channel, GuildConfiguration configuration) {
        String content = message.getContentDisplay().replaceFirst(configuration.getPrefix(), "");
        String command = content.split(" ")[0];

        Command find = commands.stream().filter(c -> c.matches(command)).findAny().orElse(null);
        if (find == null) return;

        // they have not setup
        boolean isValid = !configuration.isInvalidConfiguration() && configuration.checkConfigurationIntegrity();
        if (!isValid && !(find instanceof SetupCommand)) {
            MessageAction.send(channel, from.getAsMention() + " please setup before doing this, you can type .setup to get started.\nIf " +
                    "you already setup once then this means one of the options you set is invalid now.");
            return;
        }

        // permissions check
        boolean hasAllPermissions = PermissionCheck.hasPermission(configuration.getSelf());
        if (!hasAllPermissions) {
            EmbedBuilder embed = EmbedUtility.getSnipeEmbed();
            embed.setTitle("Missing permissions!");

            embed.addField("QueueSniper is missing some permissions, please grant them so the bot can work correctly.",
                    PermissionCheck.getPermissionsRequired(), false);
            MessageAction.send(channel, embed.build());
            return;
        }

        List<String> arguments0 = List.of(content.replace(command, "").split(" "));
        List<String> arguments = new ArrayList<>();
        arguments0.stream().filter(str -> !str.isBlank()).forEach(arguments::add);

        boolean isAdministrator = from.hasPermission(Permission.ADMINISTRATOR) || configuration.isController(from);
        TextChannel commandChannel = configuration.getCommandChannel();

        boolean isInCorrectChannel = find.isAdministratorOnly() || (channel.getId().equals(commandChannel.getId()) || isAdministrator);

        if (!isInCorrectChannel) {
            MessageAction.send(channel,
                    from.getAsMention() + " please do these type of commands in: " + commandChannel.getAsMention());
        } else {
            if (find.isAdministratorOnly() && isAdministrator) {
                find.execute(arguments, from, message, channel, configuration);
                return;
            }

            if (!find.isAdministratorOnly()) {
                find.execute(arguments, from, message, channel, configuration);
            }
        }

    }

    @SubscribeEvent
    public void onGuildMessage(GuildMessageReceivedEvent event) {
        if (event.isWebhookMessage()) return;
        Guild guild = event.getGuild();

        GuildConfiguration configuration = GuildConfigurationImpl.getConfiguration(guild.getId());
        if (configuration == null) {
            configuration = new GuildConfigurationBuilder(guild).build();
            GuildConfigurationImpl.addConfiguration(configuration);
        }

        Member from = event.getMember();
        if (configuration.isSelf(from)) return;

        Message message = event.getMessage();
        String content = message.getContentDisplay();
        if (!content.startsWith(configuration.getPrefix())) return;
        execute(message, from, event.getChannel(), configuration);
    }

    @SubscribeEvent
    public void onGuildJoin(GuildJoinEvent event) {
        GuildConfigurationImpl.addConfiguration(new GuildConfigurationBuilder(event.getGuild()).build());
    }

    @SubscribeEvent
    public void onGuildLeave(GuildLeaveEvent event) {
        GuildConfigurationImpl.removeConfiguration(event.getGuild());
    }

    @SubscribeEvent
    public void onMemberLeave(GuildMemberLeaveEvent event) {
        // unlink their account if linked.
        GuildConfigurationImpl.getConfiguration(event.getGuild().getId()).removeAccount(event.getMember().getUser().getId());
    }

}
