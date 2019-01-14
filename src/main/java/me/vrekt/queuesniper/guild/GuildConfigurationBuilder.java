package me.vrekt.queuesniper.guild;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.ArrayList;
import java.util.List;

public class GuildConfigurationBuilder {

    private final Guild guild;
    private final Member self;

    private final List<Role> controllers = new ArrayList<>();
    private final List<Role> announcers = new ArrayList<>();

    private TextChannel announcementChannel;
    private TextChannel commandChannel;
    private TextChannel matchIdChannel;
    private TextChannel matchIdEmbedChannel;
    private VoiceChannel countdownChannel;

    private int countdownDelay, channelLock;
    private String prefix;

    public GuildConfigurationBuilder(Guild guild) {
        this.guild = guild;
        this.self = guild.getSelfMember();

        prefix = ".";
    }

    public GuildConfigurationBuilder setControllers(List<Role> controllers) {
        this.controllers.addAll(controllers);
        return this;
    }

    public GuildConfigurationBuilder setAnnouncers(List<Role> announcers) {
        this.announcers.addAll(announcers);
        return this;
    }

    public GuildConfigurationBuilder setAnnouncementChannel(TextChannel channel) {
        this.announcementChannel = channel;
        return this;
    }

    public GuildConfigurationBuilder setCommandChannel(TextChannel channel) {
        this.commandChannel = channel;
        return this;
    }

    public GuildConfigurationBuilder setMatchIdChannel(TextChannel channel) {
        this.matchIdChannel = channel;
        return this;
    }

    public GuildConfigurationBuilder setMatchIdEmbedChannel(TextChannel channel) {
        this.matchIdEmbedChannel = channel;
        return this;
    }

    public GuildConfigurationBuilder setCountdownChannel(VoiceChannel channel) {
        this.countdownChannel = channel;
        return this;
    }

    public GuildConfigurationBuilder setCountdownDelay(int delay) {
        this.countdownDelay = delay;
        return this;
    }

    public GuildConfigurationBuilder setChannelLock(int lock) {
        this.channelLock = lock;
        return this;
    }

    public GuildConfigurationBuilder setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    public GuildConfiguration build() {
        return new GuildConfigurationImpl(guild, self, controllers, announcers, announcementChannel,
                commandChannel, matchIdChannel, matchIdEmbedChannel, countdownChannel, countdownDelay, channelLock, prefix);
    }

}
