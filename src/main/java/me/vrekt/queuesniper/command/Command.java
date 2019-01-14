package me.vrekt.queuesniper.command;

import me.vrekt.queuesniper.guild.GuildConfiguration;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Arrays;
import java.util.List;

public abstract class Command {

    private final String name;
    private final List<String> aliases;

    private final boolean administratorOnly;

    public Command(String name, String[] aliases, boolean administratorOnly) {
        this.name = name;
        this.aliases = Arrays.asList(aliases);
        this.administratorOnly = administratorOnly;
    }

    public boolean matches(String other) {
        return name.equalsIgnoreCase(other) || aliases.contains(other.toLowerCase());
    }

    public void execute(List<String> arguments, Member from, Message message, TextChannel channel, GuildConfiguration configuration) {
    }

    public void printUsage(Member from, TextChannel channel, String information) {
    }

    boolean isAdministratorOnly() {
        return administratorOnly;
    }

}
