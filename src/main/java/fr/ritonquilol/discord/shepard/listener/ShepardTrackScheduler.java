package fr.ritonquilol.discord.shepard.listener;

import dev.arbjerg.lavalink.client.event.TrackEndEvent;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.Message;
import fr.ritonquilol.discord.shepard.manager.GuildMusicManager;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Slf4j
public class ShepardTrackScheduler {
    private final GuildMusicManager guildMusicManager;
    public final Queue<Track> queue = new LinkedList<>();

    public ShepardTrackScheduler(GuildMusicManager guildMusicManager) {
        this.guildMusicManager = guildMusicManager;
    }

    public void enqueue(Track track) {
        this.guildMusicManager.getPlayer().ifPresentOrElse(player -> {
            if (player.getTrack() == null) {
                this.startTrack(track);
            } else {
                this.queue.offer(track);
            }
        }, () -> this.startTrack(track));
    }

    public void enqueuePlaylist(List<Track> tracks) {
        this.queue.addAll(tracks);

        this.guildMusicManager.getPlayer().ifPresentOrElse(player -> {
            if (player.getTrack() == null) {
                this.startTrack(this.queue.poll());
            }
        }, () -> this.startTrack(this.queue.poll()));
    }

    public void onTrackStart(Track track) {
        // Your homework: Send a message to the channel somehow, have fun!
        log.info("Track started: {}", track.getInfo().getTitle());
    }

    public void onTrackEnd(JDA api, TrackEndEvent event, Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason endReason) {
        if (endReason.getMayStartNext()) {
            final var nextTrack = this.queue.poll();

            if (nextTrack != null) {
                this.startTrack(nextTrack);
            } else {
                log.info("End of queue, disconnecting...");
                Guild guild = api.getGuildById(event.getGuildId());
                if (guild != null) {
                    guild.getAudioManager().closeAudioConnection();
                }
            }
        }
    }

    private void startTrack(Track track) {
        this.guildMusicManager.getLink().ifPresent(link -> link.createOrUpdatePlayer().setTrack(track).setVolume(35).subscribe());
    }
}