package me.vrekt.queuesniper.command.commands;

import me.vrekt.queuesniper.async.Concurrent;
import me.vrekt.queuesniper.command.Command;
import me.vrekt.queuesniper.command.commands.setup.ConfigurationOptionHandler;
import me.vrekt.queuesniper.embed.EmbedUtility;
import me.vrekt.queuesniper.embed.interactive.InteractiveEmbed;
import me.vrekt.queuesniper.embed.interactive.Page;
import me.vrekt.queuesniper.guild.GuildConfiguration;
import me.vrekt.queuesniper.guild.configuration.ConfigurationOptions;
import me.vrekt.queuesniper.message.MessageAction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ConfigurationCommand extends Command {

    public ConfigurationCommand() {
        super("configuration", new String[]{"settings", "config"}, true);
    }

    @Override
    public void execute(List<String> arguments, Member from, Message message, TextChannel channel, GuildConfiguration configuration) {
        EmbedBuilder embed = EmbedUtility.getSnipeEmbed().setAuthor("To select an option to change type the number!\nTo cancel type 'cancel'");

        List<Page> pages = new ArrayList<>();
        List<MessageEmbed.Field> fields = new ArrayList<>();
        List<ConfigurationOptions> options = Arrays.asList(ConfigurationOptions.values());

        AtomicInteger index = new AtomicInteger();

        options.forEach(option -> {
            index.incrementAndGet();
            String information = index.get() + ": **" + option.getName() + "**:\n*" + option.getDescription() + "*\n";
            if (option.getDefaultValue() != null) {
                information = information.concat("\n*Default value: " + option.getDefaultValue() + "*\n");
            }
            information = information.concat("*Example value: " + option.getExample() + "*\n");
            fields.add(new MessageEmbed.Field("\u200b", information, false));
        });

        ListUtils.partition(fields, 6).forEach(list -> pages.add(new Page(list)));
        fields.stream().limit(6).forEach(embed::addField);

        embed.setFooter("This embed will delete itself in 2 minutes.", null);
        try {
            channel.sendMessage(embed.build()).queue(msg -> {
                msg.addReaction("\u2B05").queue();
                msg.addReaction("\u27A1").queue();
                new ConfigurationOptionSelectionHandler(configuration, from, channel, InteractiveEmbed.register(msg, from, channel,
                        configuration, embed, pages, 2));
            });
        } catch (ErrorResponseException | InsufficientPermissionException exception) {
            //
        }
    }

    /**
     * Handles selecting and settings options for a configuration setting
     */
    public class ConfigurationOptionSelectionHandler {

        private final GuildConfiguration configuration;
        private final ConfigurationOptionHandler handler;
        private final InteractiveEmbed embed;

        private final Member from;
        private final TextChannel channel;
        private ConfigurationOptions selectedOption;

        ConfigurationOptionSelectionHandler(GuildConfiguration configuration, Member from, TextChannel channel, InteractiveEmbed embed) {
            this.configuration = configuration;
            this.from = from;
            this.channel = channel;
            handler = new ConfigurationOptionHandler(configuration, channel, from, false);
            this.embed = embed;

            JDA jda = channel.getJDA();
            jda.addEventListener(this);
            Concurrent.runAsyncLater(() -> jda.removeEventListener(this), 120000, UUID.randomUUID().toString());
        }

        @SubscribeEvent
        public void onConfigurationOptionSelection(GuildMessageReceivedEvent event) {
            if (event.isWebhookMessage()) return;
            if (!event.getGuild().getId().equals(configuration.getGuild().getId())) return;
            if (!event.getMember().getUser().getId().equals(from.getUser().getId())) return;


            // check option first to prevent previous input from being passed in as arguments
            if (selectedOption != null) {
                if (handler.handleRequest(event.getMessage(), ConfigurationOptionHandler.ReturnType.RESULT)) {
                    MessageAction.send(channel, "Option set successfully!");
                    event.getJDA().removeEventListener(this);
                    embed.delete();
                    return;
                }
            }

            String content = event.getMessage().getContentDisplay();

            if (content.equalsIgnoreCase("cancel")) {
                MessageAction.send(channel, "Cancelled.");
                event.getJDA().removeEventListener(this);
                embed.delete();
                return;
            }

            // parse content
            if (NumberUtils.isParsable(content)) {
                int index = NumberUtils.toInt(content);
                if (index > ConfigurationOptions.values().length) {
                    MessageAction.send(channel, from.getAsMention() + " not a valid selection!");
                    return;
                }

                selectedOption = ConfigurationOptions.values()[index - 1];
                handler.setOption(selectedOption);
                MessageAction.send(channel, selectedOption.getSetupString());
            } else {
                MessageAction.send(channel, from.getAsMention() + " please enter a valid number!");
            }
        }

    }

}
