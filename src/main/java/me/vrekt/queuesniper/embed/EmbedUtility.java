package me.vrekt.queuesniper.embed;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;


public class EmbedUtility {

    public static EmbedBuilder getConfigurationEmbed() {
        return new EmbedBuilder().setColor(new Color(153, 153, 255)).setTitle("CONFIGURATION OPTIONS:");
    }

    public static EmbedBuilder getSnipeEmbed() {
        return new EmbedBuilder().setColor(new Color(125, 140, 196));
    }

    public static EmbedBuilder getUsageEmbed(String command) {
        return new EmbedBuilder().setColor(new Color(102, 102, 153)).setTitle(command.toUpperCase() + " COMMAND HELP:");
    }

}
