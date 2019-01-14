package me.vrekt.queuesniper.command.commands;

import me.vrekt.queuesniper.command.Command;
import me.vrekt.queuesniper.command.commands.setup.ConfigurationOptionHandler;
import me.vrekt.queuesniper.guild.GuildConfiguration;
import me.vrekt.queuesniper.guild.GuildConfigurationImpl;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetupCommand extends Command {

    private final Map<GuildConfiguration, ConfigurationOptionHandler> monitors = new HashMap<>();

    public SetupCommand(JDA jda) {
        super("setup", new String[]{}, true);
        jda.addEventListener(this);
    }

    @Override
    public void execute(List<String> arguments, Member from, Message message, TextChannel channel, GuildConfiguration configuration) {
        monitors.put(configuration, new ConfigurationOptionHandler(configuration, channel, from, true));
    }

    @SubscribeEvent
    public void watchSetup(GuildMessageReceivedEvent event) {
        if (event.isWebhookMessage()) return;

        if (!monitors.isEmpty()) {
            TextChannel channel = event.getChannel();
            Member from = event.getMember();

            GuildConfiguration configuration = GuildConfigurationImpl.getConfiguration(event.getGuild().getId());
            if (configuration.isSelf(from)) return;
            if (!monitors.containsKey(configuration)) return;
            if (event.getMessage().getContentDisplay().contains(configuration.getPrefix() + "setup")) return;

            ConfigurationOptionHandler monitor = monitors.get(configuration);

            if (!channel.getId().equals(monitor.getChannel().getId())) return;
            if (monitors.get(configuration).handleRequest(event.getMessage(), ConfigurationOptionHandler.ReturnType.FINISHED)) {
                monitors.remove(configuration);
            }
        }

    }
}
