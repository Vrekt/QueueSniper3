package me.vrekt.queuesniper.command.commands.setup;

import me.vrekt.queuesniper.guild.GuildConfiguration;
import me.vrekt.queuesniper.guild.configuration.ConfigurationOptions;
import me.vrekt.queuesniper.message.MessageAction;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class ConfigurationOptionHandler {

    private final GuildConfiguration configuration;
    private final TextChannel channel;
    private final Member member;
    private ConfigurationOptions option;

    public ConfigurationOptionHandler(GuildConfiguration configuration, TextChannel channel, Member member, boolean send) {
        this.configuration = configuration;
        this.channel = channel;
        this.member = member;

        if (send) {
            option = ConfigurationOptions.CONTROLLERS;
            MessageAction.send(channel, option.getSetupString());
        }
    }

    public TextChannel getChannel() {
        return channel;
    }

    public void setOption(ConfigurationOptions option) {
        this.option = option;
    }

    public Member getMember() {
        return member;
    }

    /**
     * Handles setting configuration options
     *
     * @param message the message
     * @return true if setup is done.
     */
    public boolean handleRequest(Message message, ReturnType type) {
        String content = message.getContentDisplay();
        List<TextChannel> channels = message.getMentionedChannels();
        List<Role> roles = message.getMentionedRoles();

        List<String> splits = Arrays.asList(content.split(" "));

        if (option.isRole()) {
            HashSet<Role> finalRoles = getAllRoles(splits, roles, message);
            if (finalRoles == null) return false;
            handleRoleOption(option, finalRoles);
        }

        if (option.isTextChannel()) {
            if (channels.isEmpty()) {
                List<TextChannel> c = getTextChannels(splits);
                if (c.isEmpty()) {
                    MessageAction.send(channel, member.getAsMention() + " could not find those text channels!");
                    return false;
                }
                handleTextChannelOption(option, c.get(0));
            } else {
                handleTextChannelOption(option, channels.get(0));
            }
        }

        if (option.isVoiceChannel()) {
            List<VoiceChannel> vc = getVoiceChannels(content);
            if (vc.isEmpty()) {
                MessageAction.send(channel, member.getAsMention() + " the voice channel " + content + " was not found.\nEnsure it is " +
                        "typed exactly how it is displayed.");
                return false;
            }
            configuration.setCountdownChannel(vc.get(0));
        }

        if (!option.isTextChannel() && !option.isVoiceChannel() && !option.isRole()) {
            switch (option) {
                case COUNTDOWN_DELAY:
                    int delay = getInt(content);
                    if (delay == -1) return false;
                    configuration.setCountdownDelay(delay);
                    break;
                case CHANNEL_LOCK:
                    int channelLock = getInt(content);
                    if (channelLock == -1) return false;
                    configuration.setChannelLock(channelLock);
                    break;
                case PREFIX:
                    if (content.length() > 24) {
                        MessageAction.send(channel, member.getAsMention() + " I don't think its a good idea to have a prefix this " +
                                "long.");
                        return false;
                    }
                    configuration.setPrefix(content);
                    break;
            }
        }

        if (type == ReturnType.RESULT) {
            return true;
        }

        if (option.hasNext()) {
            option = option.next();
        } else {
            if (type == ReturnType.FINISHED) {
                MessageAction.send(channel, member.getAsMention() + " Setup is now complete! If you need to change any of the options " +
                        "later you can type ``" + configuration.getPrefix() + "configuration``");
                return true;
            }
        }

        if (type == ReturnType.FINISHED) {
            MessageAction.send(channel, option.getSetupString());
        }
        return false;
    }

    /**
     * Gets a list of text channels that were typed by its name.
     *
     * @param splits the string splits
     * @return a list of text channels, empty if none were found
     */
    private List<TextChannel> getTextChannels(List<String> splits) {
        List<TextChannel> channels = new ArrayList<>();
        splits.forEach(string -> {
            List<TextChannel> c = configuration.getGuild().getTextChannelsByName(string, false);
            if (!c.isEmpty()) {
                channels.addAll(c);
            } else {
                MessageAction.send(channel, member.getAsMention() + " could not find text channel: '" + string + "'");
            }
        });
        return channels;
    }

    /**
     * Gets a list of roles that were typed by its name.
     *
     * @param splits the string splits
     * @return a list of roles, empty if none were found.
     */
    private List<Role> getRoles(List<String> splits) {
        List<Role> roles = new ArrayList<>();
        splits.forEach(string -> {
            if (string.startsWith("@")) return;
            List<Role> r = configuration.getGuild().getRolesByName(string, false);
            if (!r.isEmpty()) {
                roles.addAll(r);
            } else {
                MessageAction.send(channel, member.getAsMention() + " could not find role: '" + string + "'");
            }
        });
        return roles;
    }

    /**
     * Combines mentioned roles and those that are typed.
     *
     * @param splits    the split strings
     * @param mentioned the mentioned roles
     * @return {@code null} if no roles were found.
     */
    private HashSet<Role> getAllRoles(List<String> splits, List<Role> mentioned, Message message) {
        HashSet<Role> finalRoles = new HashSet<>(mentioned);
        if (message.mentionsEveryone()) {
            finalRoles.add(message.getGuild().getPublicRole());
        }
        List<Role> text = getRoles(splits);
        finalRoles.addAll(text);
        if (finalRoles.isEmpty()) {
            MessageAction.send(channel, member.getAsMention() + " I could not find those roles! Please mention or type the name " +
                    "of the roles.");
            return null;
        }
        return finalRoles;
    }

    /**
     * Handles setting roles for certain options.
     *
     * @param option the option
     * @param roles  the roles
     */
    private void handleRoleOption(ConfigurationOptions option, HashSet<Role> roles) {
        switch (option) {
            case CONTROLLERS:
                roles.forEach(configuration::addController);
                break;
            case ANNOUNCERS:
                roles.forEach(configuration::addAnnouncer);
                break;
        }
    }

    /**
     * Handle setting text channel option
     *
     * @param option  the option
     * @param channel the text channel
     */
    private void handleTextChannelOption(ConfigurationOptions option, TextChannel channel) {
        switch (option) {
            case ANNOUNCEMENT_CHANNEL:
                configuration.setAnnouncementChannel(channel);
                break;
            case COMMAND_CHANNEL:
                configuration.setCommandChannel(channel);
                break;
            case MATCH_ID_CHANNEL:
                configuration.setMatchIdChannel(channel);
                break;
            case MATCH_ID_EMBED_CHANNEL:
                configuration.setMatchIdEmbedChannel(channel);
                break;
        }
    }

    private List<VoiceChannel> getVoiceChannels(String content) {
        return configuration.getGuild().getVoiceChannelsByName(content, false);
    }

    private int getInt(String content) {
        try {
            int i = Integer.parseInt(content);
            if (i <= 0) {
                i = 15;
            }
            return i;
        } catch (NumberFormatException exception) {
            MessageAction.send(channel, member.getAsMention() + " that is not a valid number!");
            return -1;
        }
    }

    /**
     * Finished = done with setup
     * Result = setting role/text channel/whatever else was successful
     */
    public enum ReturnType {
        FINISHED, RESULT
    }

}
