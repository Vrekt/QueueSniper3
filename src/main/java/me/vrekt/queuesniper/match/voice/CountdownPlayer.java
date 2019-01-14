package me.vrekt.queuesniper.match.voice;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import me.vrekt.queuesniper.QueueSniperBot;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

public class CountdownPlayer {

    private static final AudioPlayerManager PLAYER_MANAGER = new DefaultAudioPlayerManager();
    private static final int BROADCAST_VOLUME = 50;

    static {
        AudioSourceManagers.registerLocalSource(PLAYER_MANAGER);
    }

    public static void countdown(Guild guild, VoiceChannel channel, String audio) {
        final AudioPlayer player = PLAYER_MANAGER.createPlayer();
        SendHandler sendHandler = new SendHandler(player);

        final AudioManager manager = guild.getAudioManager();

        // Close the audio connection and destroy player once the track is finished
        player.addListener(new AudioEventAdapter() {
            @Override
            public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                manager.closeAudioConnection();
                manager.setSendingHandler(null);
                player.destroy();
            }
        });

        PLAYER_MANAGER.loadItem(QueueSniperBot.DIRECTORY + audio, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                manager.setSendingHandler(sendHandler);
                manager.openAudioConnection(channel);

                player.setVolume(BROADCAST_VOLUME);
                player.playTrack(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {

            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException exception) {
                exception.printStackTrace();
            }
        });
    }

    private static class SendHandler implements AudioSendHandler {

        private final AudioPlayer audioPlayer;
        private AudioFrame lastFrame;

        SendHandler(AudioPlayer audioPlayer) {
            this.audioPlayer = audioPlayer;
        }

        @Override
        public boolean canProvide() {
            lastFrame = audioPlayer.provide();
            return lastFrame != null;
        }

        @Override
        public byte[] provide20MsAudio() {
            return lastFrame.getData();
        }

        @Override
        public boolean isOpus() {
            return true;
        }

    }

}
