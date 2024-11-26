package fr.ritonquilol.discord.shepard.helper;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;

import java.nio.ByteBuffer;

public class AudioPlayerSendHandler implements AudioSendHandler {
    private final AudioPlayer audioPlayer;
    private final ByteBuffer buffer;
    private MutableAudioFrame mutableAudioFrame;

    public AudioPlayerSendHandler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.buffer = ByteBuffer.allocate(1024);
        this.mutableAudioFrame = new MutableAudioFrame();
        mutableAudioFrame.setBuffer(buffer);
    }

    @Override
    public boolean canProvide() {
        return audioPlayer.provide(mutableAudioFrame);
    }

    @Override
    public ByteBuffer provide20MsAudio() {
        return buffer.flip();
    }

    @Override
    public boolean isOpus() {
        return true;  // Lavaplayer fournit de l'audio en format Opus
    }
}
