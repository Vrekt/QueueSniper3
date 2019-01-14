package me.vrekt.queuesniper.guild;

import io.github.robertograham.fortnite2.domain.Account;
import io.github.robertograham.fortnite2.domain.enumeration.Platform;
import me.vrekt.queuesniper.guild.yaml.YamlConfiguration;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

public interface GuildConfiguration {

    Guild getGuild();

    Member getSelf();

    boolean isSelf(Member from);

    String buildControllers();

    void setControllers(List<Role> controllers);

    void addController(Role controller);

    boolean isController(Member from);

    String buildAnnouncers();

    void addAnnouncer(Role announcer);

    HashSet<Role> getAnnouncers();

    void setAnnouncers(List<Role> announcers);

    Map<String, Map<Account, Platform>> getFortniteAccounts();

    boolean hasLinked(String memberId);

    void addAccount(String memberId, Account account, Platform platform);

    void removeAccount(String memberId);

    String getAccountName(String memberId);

    Platform getPlatform(String memberId, Account account);

    Account getAccount(String memberId);

    TextChannel getAnnouncementChannel();

    void setAnnouncementChannel(TextChannel channel);

    TextChannel getCommandChannel();

    void setCommandChannel(TextChannel channel);

    TextChannel getMatchIdChannel();

    void setMatchIdChannel(TextChannel channel);

    TextChannel getMatchIdEmbedChannel();

    void setMatchIdEmbedChannel(TextChannel channel);

    VoiceChannel getCountdownChannel();

    void setCountdownChannel(VoiceChannel channel);

    int getCountdownDelay();

    void setCountdownDelay(int delay);

    int getChannelLock();

    void setChannelLock(int lock);

    String getPrefix();

    void setPrefix(String prefix);

    boolean isInvalidConfiguration();

    boolean checkConfigurationIntegrity();

    YamlConfiguration toYaml();

}
