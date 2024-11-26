package fr.ritonquilol.discord.shepard.listener;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ShepardTrackScheduler extends AudioEventAdapter {

    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;

    public ShepardTrackScheduler(AudioPlayer player){
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track){
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            nextTrack();
        } else {
            player.destroy();
        }
    }

    public void nextTrack(){
        player.startTrack(queue.poll(), true);
    }
}
