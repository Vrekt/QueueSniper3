package me.vrekt.queuesniper.embed.interactive;

import me.vrekt.queuesniper.async.Concurrent;
import me.vrekt.queuesniper.embed.EmbedUtility;
import me.vrekt.queuesniper.guild.GuildConfiguration;
import me.vrekt.queuesniper.message.MessageAction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class InteractiveEmbed {

    private final List<Page> pages = new ArrayList<>();
    private final Message message;
    private final Member member;
    private final TextChannel channel;
    private final GuildConfiguration configuration;
    private final long id;

    private EmbedBuilder embed;
    private Page current;

    private InteractiveEmbed(Message message, Member from, TextChannel channel, GuildConfiguration configuration) {
        this.message = message;
        this.member = from;
        this.channel = channel;
        this.configuration = configuration;
        id = System.currentTimeMillis();

        Concurrent.runAsyncLater(this::delete, Duration.of(5, ChronoUnit.MINUTES).toMillis(), id);
        channel.getJDA().addEventListener(this);
    }

    private InteractiveEmbed(Message message, Member from, TextChannel channel, GuildConfiguration configuration, EmbedBuilder embed) {
        this(message, from, channel, configuration);
        this.embed = embed;
    }

    public static InteractiveEmbed register(Message message, Member from, TextChannel channel, GuildConfiguration configuration, EmbedBuilder embed, List<Page> pages) {
        InteractiveEmbed interactiveEmbed = new InteractiveEmbed(message, from, channel, configuration, embed);
        interactiveEmbed.addPages(pages);
        interactiveEmbed.setCurrent(pages.get(0));
        return interactiveEmbed;
    }

    private void addPages(List<Page> pages) {
        this.pages.addAll(pages);
    }

    private void setCurrent(Page current) {
        this.current = current;
    }

    @SubscribeEvent
    public void onReactionAdd(GuildMessageReactionAddEvent event) {
        handleReaction(event.getChannel(), event.getMember(), event.getReaction());
    }

    @SubscribeEvent
    public void onReactionRemove(GuildMessageReactionRemoveEvent event) {
        handleReaction(event.getChannel(), event.getMember(), event.getReaction());
    }

    /**
     * Called when a reaction is removed or added
     *
     * @param eventChannel the channel
     * @param from         who it was from
     * @param emote        the emote
     */
    private void handleReaction(TextChannel eventChannel, Member from, MessageReaction emote) {
        if (!eventChannel.getId().equals(channel.getId())) return;

        if (configuration.isSelf(from)) return;
        if (!from.getUser().getId().equals(member.getUser().getId())) {
            try {
                emote.removeReaction(from.getUser()).queue();
            } catch (InsufficientPermissionException exception) {
                //
            }
            return;
        }

        if (current == null) return;
        int index = pages.indexOf(current);

        String reaction = emote.getReactionEmote().getName();
        if (reaction.equals("➡")) {
            if (index + 1 >= pages.size()) return;

            index++;
            current = pages.get(index);
        }

        if (reaction.equals("⬅")) {
            if (index - 1 < 0) return;

            index--;
            current = pages.get(index);
        }

        EmbedBuilder embed = current.addAllPages(this.embed == null ? EmbedUtility.getSnipeEmbed() : this.embed.clearFields());
        embed.setFooter("Current page: " + "[" + (index + 1) + "/" + pages.size() + "] (This embed will delete itself in 5 minutes.)",
                from.getUser().getAvatarUrl());
        MessageAction.edit(channel, message.getId(), embed.build());
    }

    /**
     * General cleanup basically
     */
    public void delete() {

        Concurrent.cancelTask(id);
        pages.clear();
        current = null;
        MessageAction.delete(channel, message.getId());
        channel.getJDA().removeEventListener(this);
    }

}
