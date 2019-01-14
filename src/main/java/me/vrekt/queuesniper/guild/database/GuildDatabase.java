package me.vrekt.queuesniper.guild.database;

import me.vrekt.queuesniper.QueueSniperBot;
import me.vrekt.queuesniper.async.Concurrent;
import me.vrekt.queuesniper.fortnite.AccountAPI;
import me.vrekt.queuesniper.guild.GuildConfiguration;
import me.vrekt.queuesniper.guild.GuildConfigurationBuilder;
import me.vrekt.queuesniper.guild.GuildConfigurationImpl;
import me.vrekt.queuesniper.guild.yaml.YamlConfiguration;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import org.apache.commons.lang3.ObjectUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GuildDatabase {

    /**
     * Attempts to load the database within the specified time.
     *
     * @param file    the file
     * @param jda     jda
     * @param timeout the timeout in seconds
     * @return true if loading succeeded
     */
    public static boolean loadWithin(String file, JDA jda, int timeout) {
        Future<Boolean> result = Executors.newSingleThreadExecutor().submit(() -> load(file, jda));

        try {
            return result.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException exception) {
            return false;
        }
    }

    /**
     * Attempts to load the database within the specified time.
     *
     * @param file the file
     * @param jda  jda
     * @return true if loading succeeded
     */
    private static boolean load(String file, JDA jda) {
        if (wasFileCreated(file)) return true;

        jda.getGuilds().forEach(guild -> {
            GuildConfiguration config = GuildConfigurationImpl.getConfiguration(guild.getId());
            if (config == null) {
                GuildConfigurationImpl.addConfiguration(new GuildConfigurationBuilder(guild).build());
            }
        });

        long now = System.currentTimeMillis();
        try (InputStream in = new FileInputStream(file)) {
            Yaml yaml = new Yaml();

            Map<String, Object> data = yaml.load(in);
            if (data == null || data.isEmpty()) {
                return true;
            }

            for (String guildId : data.keySet()) {
                Object obj = data.get(guildId);
                if (obj instanceof YamlConfiguration) {
                    YamlConfiguration config = (YamlConfiguration) obj;
                    Guild guild = jda.getGuildById(guildId);
                    if (guild == null) {
                        continue;
                    }

                    GuildConfigurationBuilder builder = new GuildConfigurationBuilder(guild);

                    List<Role> controllers = new ArrayList<>();
                    List<Role> announcers = new ArrayList<>();

                    config.controllers.forEach(id -> controllers.add(guild.getRoleById(id)));
                    config.announcers.forEach(id -> announcers.add(guild.getRoleById(id)));

                    TextChannel announcementChannel = guild.getTextChannelById(config.announcementChannelId);
                    TextChannel commandChannel = guild.getTextChannelById(config.commandChannelId);
                    TextChannel matchIdChannel = guild.getTextChannelById(config.matchIdChannel);
                    TextChannel matchIdEmbedChannel = guild.getTextChannelById(config.matchIdEmbedChannel);
                    VoiceChannel countdownChannel = guild.getVoiceChannelById(config.countdownChannel);

                    String prefix = config.prefix;

                    int countdownDelay = config.countdownDelay, channelLock = config.channelLock;

                    if (controllers.stream().anyMatch(Objects::isNull) || announcers.stream().anyMatch(Objects::isNull) ||
                            !ObjectUtils.allNotNull(announcementChannel, commandChannel, matchIdChannel, matchIdEmbedChannel, countdownChannel)) {
                        GuildConfigurationImpl.addConfiguration(builder.build());
                        continue;
                    }

                    GuildConfiguration configuration = builder.setControllers(controllers).setAnnouncers(announcers)
                            .setAnnouncementChannel(announcementChannel).setCommandChannel(commandChannel)
                            .setMatchIdChannel(matchIdChannel).setMatchIdEmbedChannel(matchIdEmbedChannel).setCountdownChannel(countdownChannel)
                            .setCountdownDelay(countdownDelay).setChannelLock(channelLock).setPrefix(prefix).build();

                    Map<String, Map<String, String>> fortniteAccounts = config.fortniteAccounts;
                    GuildConfigurationImpl.addConfiguration(configuration);

                    Concurrent.runAsync(() -> AccountAPI.loadAccounts(configuration, fortniteAccounts));
                }
            }

        } catch (IOException exception) {
            return false;
        }

        long elapsed = System.currentTimeMillis() - now;
        System.out.println("Finished loading guilds in: " + elapsed + "ms.");
        return true;
    }

    /**
     * Attempts to save the database within the specified time
     *
     * @param file    the file
     * @param timeout the timeout in seconds
     * @return false if saving failed
     */
    public static boolean saveWithin(String file, int timeout) {
        Future<Boolean> result = Executors.newSingleThreadExecutor().submit(() -> save(file));

        try {
            return result.get(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException exception) {
            return false;
        }
    }

    /**
     * Attempts to save the database
     *
     * @param file the file
     * @return false if saving failed
     */
    private static boolean save(String file) {
        if (wasFileCreated(file)) return true;

        long now = System.currentTimeMillis();
        try (FileWriter out = new FileWriter(file, false)) {

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);

            Yaml yaml = new Yaml(options);
            Map<String, GuildConfiguration> configurations = GuildConfigurationImpl.getConfigurationMap();
            if (configurations.isEmpty()) {
                out.close();
                return true;
            }

            Map<String, YamlConfiguration> dump = new HashMap<>();
            configurations.forEach((key, value) -> {
                YamlConfiguration configuration = value.toYaml();
                if (configuration == null) {
                    return;
                }
                dump.put(key, configuration);
            });

            yaml.dump(dump, out);
        } catch (IOException exception) {
            return false;
        }

        long elapsed = System.currentTimeMillis() - now;
        System.out.println("Finished saving guilds in: " + elapsed + "ms.");
        return true;
    }

    /**
     * @param fileLocation the location of the file
     * @return true if the file was created
     */
    private static boolean wasFileCreated(String fileLocation) {
        File file = new File(fileLocation);
        try {
            if (file.createNewFile()) {
                return true;
            }
        } catch (IOException exception) {
            return false;
        }
        return false;
    }

    /**
     * Saves all the guilds in a file with the current time in milliseconds.
     */
    public static void backup() {
        long now = System.currentTimeMillis();

        String backup = QueueSniperBot.DIRECTORY + "guilds" + now + ".yaml";
        if (wasFileCreated(backup)) {
            System.out.println("Backing up guilds database, this may take awhile.");
            saveWithin(backup, 1800);
            long elapsed = System.currentTimeMillis() - now;
            System.out.println("Finished! Took: " + elapsed + "ms");
        }
    }

}
