package fr.ritonquilol.discord.shepard.service;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.ritonquilol.discord.shepard.manager.GuildMusicManager;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class MusicService {

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    public MusicService() {
        this.playerManager = new DefaultAudioPlayerManager();
        this.musicManagers = new HashMap<>();
        AudioSourceManagers.registerRemoteSources(playerManager);  // Pour YouTube, SoundCloud, etc.
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public synchronized GuildMusicManager getGuildMusicManager(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
            guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
        }

        return musicManager;
    }

    public void loadAndPlay(Guild guild, String trackUrl) {
        GuildMusicManager musicManager = getGuildMusicManager(guild);

        playerManager.loadItem(trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                musicManager.scheduler.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks()) {
                    musicManager.scheduler.queue(track);
                }
            }

            @Override
            public void noMatches() {
                System.out.println("Pas de correspondance pour : " + trackUrl);
            }

            @Override
            public void loadFailed(FriendlyException e) {
                System.out.println("Erreur de chargement de la piste : " + e.getMessage());
            }
        });
    }
}
