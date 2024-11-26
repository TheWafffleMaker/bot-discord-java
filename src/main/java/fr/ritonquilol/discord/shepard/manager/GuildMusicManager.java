package fr.ritonquilol.discord.shepard.manager;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import fr.ritonquilol.discord.shepard.helper.AudioPlayerSendHandler;
import fr.ritonquilol.discord.shepard.listener.ShepardTrackScheduler;

public class GuildMusicManager {
    public final AudioPlayer player;
    public final ShepardTrackScheduler scheduler;

    public GuildMusicManager(AudioPlayerManager manager) {
        this.player = manager.createPlayer();
        this.scheduler = new ShepardTrackScheduler(player);
        this.player.addListener(scheduler);
    }

    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(player);
    }
}
