package me.vrekt.queuesniper.message;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

public class MessageAction {

    public static boolean send(TextChannel channel, String message) {
        if (channel == null || message == null) {
            return false;
        }

        try {
            channel.sendMessage(message).queue();
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

    public static boolean send(TextChannel channel, MessageEmbed message) {
        if (channel == null || message == null) {
            return false;
        }

        try {
            channel.sendMessage(message).queue();
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

    public static boolean delete(TextChannel channel, String messageId) {
        if (channel == null || messageId == null) {
            return false;
        }

        try {
            channel.deleteMessageById(messageId).queue();
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

    public static boolean edit(TextChannel channel, String messageId, MessageEmbed embed) {
        if (channel == null || messageId == null) {
            return false;
        }

        try {
            channel.editMessageById(messageId, embed).queue();
        } catch (Exception exception) {
            return false;
        }
        return true;
    }

}
