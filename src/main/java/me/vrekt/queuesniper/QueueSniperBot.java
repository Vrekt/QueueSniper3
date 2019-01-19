package me.vrekt.queuesniper;

import me.vrekt.queuesniper.async.Concurrent;
import me.vrekt.queuesniper.command.CommandExecutor;
import me.vrekt.queuesniper.fortnite.AccountAPI;
import me.vrekt.queuesniper.guild.database.GuildDatabase;
import me.vrekt.queuesniper.match.MatchQueue;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.net.URISyntaxException;
import java.security.CodeSource;

public class QueueSniperBot {

    public static String DIRECTORY;

    private String username, password;

    private QueueSniperBot(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Arguments: <token> <epic username> <epic password>");
            return;
        }

        // get running directory
        try {
            CodeSource source = QueueSniperBot.class.getProtectionDomain().getCodeSource();
            DIRECTORY = new File(source.getLocation().toURI().getPath()).getParentFile().getPath() + File.separator;
        } catch (URISyntaxException exception) {
            exception.printStackTrace();
            return;
        }

        String token = args[0];
        String username = args[1];
        String password = args[2];

        try {
            new JDABuilder(AccountType.BOT).setToken(token).setEventManager(new AnnotatedEventManager()).addEventListeners(new QueueSniperBot(username, password)).build();
        } catch (LoginException exception) {
            System.out.println("Invalid token.");
        }
    }

    @SubscribeEvent
    public void whenReady(ReadyEvent event) {
        JDA jda = event.getJDA();

        Concurrent.start();

        boolean result = AccountAPI.buildFortnite(username, password);

        load(jda);
        MatchQueue.start();

        new CommandExecutor(jda, result);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            save();
            AccountAPI.close();
        }));
    }

    /**
     * loads the database
     *
     * @param jda jda..
     */
    private void load(JDA jda) {
        boolean guildsLoad = GuildDatabase.loadWithin(DIRECTORY + "guilds.yaml", jda, 60);
        if (!guildsLoad) {
            System.out.println("Could not load guilds! Shutting down.");
            Runtime.getRuntime().halt(0);
        }
    }

    /**
     * Saves the database
     */
    private void save() {
        boolean guildSave = GuildDatabase.saveWithin(DIRECTORY + "guilds.yaml", 60);
        if (!guildSave) {
            System.out.println("Could not save guilds.");
        }
    }

}
