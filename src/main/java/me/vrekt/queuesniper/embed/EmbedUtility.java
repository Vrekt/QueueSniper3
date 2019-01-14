package me.vrekt.queuesniper.embed;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;


public class EmbedUtility {

    public static EmbedBuilder getSnipeEmbed() {
        return new EmbedBuilder().setColor(new Color(125, 140, 196));
    }

}
