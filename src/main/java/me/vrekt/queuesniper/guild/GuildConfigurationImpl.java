package me.vrekt.queuesniper.guild;

import io.github.robertograham.fortnite2.domain.Account;
import io.github.robertograham.fortnite2.domain.enumeration.Platform;
import me.vrekt.queuesniper.guild.yaml.YamlConfiguration;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class GuildConfigurationImpl implements GuildConfiguration {

    private static final Map<String, GuildConfiguration> CONFIGURATION_MAP = new HashMap<>();

    private final Guild guild;
    private final Member self;

    private final Map<String, Map<Account, Platform>> fortniteAccounts = new ConcurrentHashMap<>();
    private final HashSet<Role> controllers = new HashSet<>();
    private final HashSet<Role> announcers = new HashSet<>();
    private TextChannel announcementChannel;
    private TextChannel commandChannel;
    private TextChannel matchIdChannel;
    private TextChannel matchIdEmbedChannel;
    private VoiceChannel countdownChannel;
    private int countdownDelay, channelLock;
    private String prefix;

    GuildConfigurationImpl(Guild guild, Member self, List<Role> controllers, List<Role> announcers,
                           TextChannel announcementChannel, TextChannel commandChannel, TextChannel matchIdChannel,
                           TextChannel matchIdEmbedChannel, VoiceChannel countdownChannel,
                           int countdownDelay, int channelLock, String prefix) {
        this.guild = guild;
        this.self = self;

        this.controllers.addAll(controllers);
        this.announcers.addAll(announcers);
        this.announcementChannel = announcementChannel;
        this.commandChannel = commandChannel;
        this.matchIdChannel = matchIdChannel;
        this.matchIdEmbedChannel = matchIdEmbedChannel;
        this.countdownChannel = countdownChannel;
        this.countdownDelay = countdownDelay;
        this.channelLock = channelLock;
        this.prefix = prefix;
    }

    public static GuildConfiguration getConfiguration(String guildId) {
        return CONFIGURATION_MAP.getOrDefault(guildId, null);
    }

    public static void addConfiguration(GuildConfiguration configuration) {
        CONFIGURATION_MAP.put(configuration.getGuild().getId(), configuration);
    }

    public static void removeConfiguration(Guild guild) {
        CONFIGURATION_MAP.remove(guild.getId());
    }

    public static Map<String, GuildConfiguration> getConfigurationMap() {
        return CONFIGURATION_MAP;
    }

    @Override
    public Guild getGuild() {
        return guild;
    }

    @Override
    public Member getSelf() {
        return self;
    }

    @Override
    public boolean isSelf(Member from) {
        return from.getUser().getId().equals(self.getUser().getId());
    }

    @Override
    public String buildControllers() {
        StringBuilder mentions = new StringBuilder();
        controllers.forEach(role -> {
            mentions.append(role.getAsMention());
            mentions.append(" ");
        });
        return mentions.toString();
    }

    @Override
    public void setControllers(List<Role> controllers) {
        this.controllers.addAll(controllers);
    }

    @Override
    public void addController(Role controller) {
        this.controllers.add(controller);
    }

    @Override
    public boolean isController(Member from) {
        return from.getRoles().stream().anyMatch(controllers::contains);
    }

    @Override
    public String buildAnnouncers() {
        StringBuilder mentions = new StringBuilder();
        announcers.forEach(role -> {
            mentions.append(role.getAsMention());
            mentions.append(" ");
        });
        return mentions.toString();
    }

    @Override
    public HashSet<Role> getAnnouncers() {
        return announcers;
    }

    @Override
    public void setAnnouncers(List<Role> announcers) {
        this.announcers.addAll(announcers);
    }

    @Override
    public void addAnnouncer(Role announcer) {
        this.announcers.add(announcer);
    }

    @Override
    public Map<String, Map<Account, Platform>> getFortniteAccounts() {
        return fortniteAccounts;
    }

    @Override
    public boolean hasLinked(String memberId) {
        return fortniteAccounts.containsKey(memberId);
    }

    @Override
    public void addAccount(String memberId, Account account, Platform platform) {
        fortniteAccounts.put(memberId, new HashMap<>());
        fortniteAccounts.get(memberId).put(account, platform);
    }

    @Override
    public void removeAccount(String memberId) {
        fortniteAccounts.remove(memberId);
    }

    @Override
    public String getAccountName(String memberId) {
        if (!fortniteAccounts.containsKey(memberId)) return null;
        Optional<Account> account = fortniteAccounts.get(memberId).keySet().stream().findFirst();
        return account.map(Account::displayName).orElse(null);
    }

    @Override
    public Platform getPlatform(String memberId, Account account) {
        return fortniteAccounts.get(memberId).get(account);
    }

    @Override
    public Account getAccount(String memberId) {
        return fortniteAccounts.get(memberId).keySet().stream().findFirst().orElse(null);
    }

    @Override
    public TextChannel getAnnouncementChannel() {
        return announcementChannel;
    }

    @Override
    public void setAnnouncementChannel(TextChannel channel) {
        this.announcementChannel = channel;
    }

    @Override
    public TextChannel getCommandChannel() {
        return commandChannel;
    }

    @Override
    public void setCommandChannel(TextChannel channel) {
        this.commandChannel = channel;
    }

    @Override
    public TextChannel getMatchIdChannel() {
        return matchIdChannel;
    }

    @Override
    public void setMatchIdChannel(TextChannel channel) {
        this.matchIdChannel = channel;
    }

    @Override
    public TextChannel getMatchIdEmbedChannel() {
        return matchIdEmbedChannel;
    }

    @Override
    public void setMatchIdEmbedChannel(TextChannel channel) {
        this.matchIdEmbedChannel = channel;
    }

    @Override
    public VoiceChannel getCountdownChannel() {
        return countdownChannel;
    }

    @Override
    public void setCountdownChannel(VoiceChannel channel) {
        this.countdownChannel = channel;
    }

    @Override
    public int getCountdownDelay() {
        return countdownDelay;
    }

    @Override
    public void setCountdownDelay(int delay) {
        this.countdownDelay = delay;
    }

    @Override
    public int getChannelLock() {
        return channelLock;
    }

    @Override
    public void setChannelLock(int lock) {
        this.channelLock = lock;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * @return true if any of the required channels/roles are null/empty.
     */
    @Override
    public boolean isInvalidConfiguration() {
        return controllers.isEmpty() || announcers.isEmpty() || announcementChannel == null || commandChannel == null ||
                matchIdChannel == null || matchIdEmbedChannel == null || countdownChannel == null;
    }

    /**
     * @return true if none of the channels/roles are invalid.
     */
    @Override
    public boolean checkConfigurationIntegrity() {
        AtomicBoolean result = new AtomicBoolean(true);
        controllers.forEach(role -> result.set(guild.getRoles().contains(role) && (result.get())));
        announcers.forEach(role -> result.set(guild.getRoles().contains(role) && (result.get())));
        result.set(guild.getTextChannels().contains(announcementChannel) && (result.get()));
        result.set(guild.getTextChannels().contains(commandChannel) && (result.get()));
        result.set(guild.getTextChannels().contains(matchIdChannel) && (result.get()));
        result.set(guild.getTextChannels().contains(matchIdEmbedChannel) && (result.get()));
        result.set(guild.getVoiceChannels().contains(countdownChannel) && (result.get()));
        return result.get();
    }

    @Override
    public YamlConfiguration toYaml() {
        try {
            YamlConfiguration configuration = new YamlConfiguration();
            if (isInvalidConfiguration()) return null;
            // just check if we are not setup, since invalid channels will be caught when the database loads again.

            configuration.guildId = guild.getId();

            List<String> controllers = new ArrayList<>();
            List<String> announcers = new ArrayList<>();

            this.controllers.forEach(controller -> controllers.add(controller.getId()));
            this.announcers.forEach(announcer -> announcers.add(announcer.getId()));
            configuration.controllers = controllers;
            configuration.announcers = announcers;

            Map<String, Map<String, String>> fortniteAccounts = new HashMap<>();
            this.fortniteAccounts.forEach((member, map) -> {
                fortniteAccounts.put(member, new HashMap<>());
                map.forEach((id, platform) -> fortniteAccounts.get(member).put(id.accountId(), platform.name()));
            });

            configuration.fortniteAccounts = fortniteAccounts;

            configuration.announcementChannelId = announcementChannel.getId();
            configuration.commandChannelId = commandChannel.getId();
            configuration.matchIdChannel = matchIdChannel.getId();
            configuration.matchIdEmbedChannel = matchIdEmbedChannel.getId();
            configuration.countdownChannel = countdownChannel.getId();
            configuration.prefix = prefix;

            configuration.countdownDelay = countdownDelay;
            configuration.channelLock = channelLock;
            return configuration;
        } catch (Exception exception) {
            // catch any weird exceptions that might occur so the thread doesn't get fucked over
        }
        return null;
    }
}
