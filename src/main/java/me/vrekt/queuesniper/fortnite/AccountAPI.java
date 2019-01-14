package me.vrekt.queuesniper.fortnite;

import io.github.robertograham.fortnite2.client.Fortnite;
import io.github.robertograham.fortnite2.domain.Account;
import io.github.robertograham.fortnite2.domain.Statistic;
import io.github.robertograham.fortnite2.domain.enumeration.PartyType;
import io.github.robertograham.fortnite2.domain.enumeration.Platform;
import io.github.robertograham.fortnite2.implementation.DefaultFortnite;
import me.vrekt.queuesniper.embed.EmbedUtility;
import me.vrekt.queuesniper.embed.interactive.InteractiveEmbed;
import me.vrekt.queuesniper.embed.interactive.Page;
import me.vrekt.queuesniper.guild.GuildConfiguration;
import me.vrekt.queuesniper.message.MessageAction;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import org.apache.commons.collections4.ListUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AccountAPI {

    /**
     * Credits to: https://github.com/RobertoGraham/fortnite-2 for the Fortnite stats API.
     */

    private static Fortnite fortnite;

    /**
     * Builds fortnite
     *
     * @param username the username
     * @param password the password
     * @return true if building was successful and did not throw an error.
     */
    public static boolean buildFortnite(String username, String password) {
        try {
            DefaultFortnite.Builder builder = DefaultFortnite.Builder.newInstance(username, password);
            fortnite = builder.build();
        } catch (IllegalStateException exception) {
            return false;
        }
        return true;
    }

    /**
     * Gets an account if it exists and attempts to set it in the config
     *
     * @param from          who it was sent from
     * @param channel       the channel it was sent in
     * @param name          the name of the account
     * @param platform      their platform
     * @param configuration the guild it was sent from
     */
    public static void getAccountAndSetIfExists(Member from, TextChannel channel, String name,
                                                Platform platform, GuildConfiguration configuration) {
        try {
            if (configuration.hasLinked(from.getUser().getId())) {
                MessageAction.send(channel, from.getAsMention() + " you have already linked your account!");
                return;
            }

            Optional<Account> account = fortnite.account().findOneByDisplayName(name);
            if (account.isEmpty()) {
                MessageAction.send(channel, from.getAsMention() + " could not find your account!");
                return;
            }

            EmbedBuilder embed = EmbedUtility.getSnipeEmbed();
            embed.setFooter("Your fortnite account is now linked!\nIf you ever need to unlink your account do !unlink", null);
            MessageAction.send(channel, embed.build());
            configuration.addAccount(from.getUser().getId(), account.get(), platform);
        } catch (IOException exception) {
            MessageAction.send(channel, from.getAsMention() + " could not find your account!");
        }
    }

    /**
     * Shows stats for a player
     *
     * @param from          who it was sent from
     * @param channel       the channel it was sent in
     * @param account       their account
     * @param configuration the guild it was sent from
     */
    public static void showStats(Member from, TextChannel channel, Account account, GuildConfiguration configuration) {
        EmbedBuilder embed = EmbedUtility.getSnipeEmbed();

        embed.setTitle("Showing stats for: " + account.displayName() + " (Not all time, current season)");
        List<Page> pages = new ArrayList<>();
        List<MessageEmbed.Field> fields = new ArrayList<>();

        Platform platform = configuration.getPlatform(from.getUser().getId(), account);

        for (PartyType type : PartyType.values()) {
            try {
                Statistic statistic =
                        fortnite.statistic().findAllByAccountForCurrentSeason(account).map(filterableStatistic ->
                                filterableStatistic.byPlatform(platform).byPartyType(type)).orElse(null);

                if (statistic == null) continue;

                String stats = "\n**Basic information:**\nTotal wins: " + statistic.wins() + "\nTotal kills: " + statistic.kills() +
                        "\nScore: " + statistic.score() + "\n\n**Match information:**\nTotal matches played: " + statistic.matches() +
                        "\nTimes you placed top 3: " + statistic.timesPlacedTop3() + "\nTimes you placed top 5: "
                        + statistic.timesPlacedTop5() + "\nTimes you placed top 10: " + statistic.timesPlacedTop10();

                fields.add(new MessageEmbed.Field(type.name(), stats, false));
            } catch (IOException exception) {
                //
            }
        }

        ListUtils.partition(fields, 1).forEach(list -> pages.add(new Page(list)));
        fields.stream().limit(1).forEach(embed::addField);

        embed.setFooter("This embed will delete itself in 5 minutes.", null);
        try {
            channel.sendMessage(embed.build()).queue(msg -> {
                msg.addReaction("\u2B05").queue();
                msg.addReaction("\u27A1").queue();
                InteractiveEmbed.register(msg, from, channel, configuration, embed, pages, 5);
            });
        } catch (ErrorResponseException | InsufficientPermissionException exception) {
            //
        }

    }

    /**
     * Loads the accounts that were saved to this config
     *
     * @param configuration the guild
     * @param accounts      the accounts
     */
    public static void loadAccounts(GuildConfiguration configuration, Map<String, Map<String, String>> accounts) {
        if (fortnite == null) return;

        accounts.forEach((member, map) -> map.forEach((id, platform) -> {
            try {
                fortnite.account().findAllByAccountIds(id).ifPresent(a -> a.stream().findFirst()
                        .ifPresent(account -> configuration.addAccount(member, account, Platform.valueOf(platform))));
            } catch (IOException exception) {
                //
            }
        }));
    }

    /**
     * Closes the fortnite instance
     */
    public static void close() {
        fortnite.close();
    }

}
