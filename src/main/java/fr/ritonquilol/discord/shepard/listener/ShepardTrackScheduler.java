package fr.ritonquilol.discord.shepard.listener;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class ShepardTrackScheduler extends AudioEventAdapter {

    private final AudioPlayer player;

    public ShepardTrackScheduler(AudioPlayer player){
        this.player = player;
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {

    }
}
