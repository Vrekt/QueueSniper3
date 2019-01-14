package me.vrekt.queuesniper.embed.interactive;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.List;

public class Page {

    private final List<MessageEmbed.Field> fields;

    public Page(List<MessageEmbed.Field> fields) {
        this.fields = fields;
    }

    EmbedBuilder addAllPages(EmbedBuilder embed) {
        fields.forEach(embed::addField);
        return embed;
    }

}
