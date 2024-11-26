package fr.ritonquilol.discord.shepard.listener;

import fr.ritonquilol.discord.shepard.command.PlayCommand;
import fr.ritonquilol.discord.shepard.service.MusicService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class ShepardListener extends ListenerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShepardListener.class);
    final PlayCommand playCommand;
    private final MusicService musicService;

    public ShepardListener(PlayCommand playCommand, MusicService musicService) {
        this.playCommand = playCommand;
        this.musicService = musicService;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String authorName = event.getAuthor().getName();
        String messageContent = event.getMessage().getContentRaw();

        LOGGER.debug("Received message : [{}] from [{}]", messageContent, authorName);
        if (event.getAuthor().isBot()) {
            LOGGER.debug("Unhandled message from bot [{}]", authorName);
            return;
        }

        if ("!ping".equals(messageContent)) {
            MessageChannel channel = event.getChannel();
            channel.sendMessage("Pong!").queue();
        }

        if (messageContent.startsWith("!play")) {
            String link = messageContent.substring(6);  // Extraction du lien
            Guild guild = event.getGuild();

            // Connexion au salon vocal
            AudioChannel channel = event.getMember().getVoiceState().getChannel();
            if (channel != null) {
                guild.getAudioManager().openAudioConnection(channel);
                musicService.loadAndPlay(guild, link);  // Utilisation du service pour jouer la musique
            } else {
                event.getChannel().sendMessage("Tu dois être dans un salon vocal pour jouer de la musique !").queue();
            }
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        LOGGER.info("Slash command interaction : {}", event.getName());
        switch (event.getName()) {
            case "zoui" -> event.reply("ZOUI").queue();
            case "zoum" -> event.reply("ZOUMIZ").queue();
            case "play" -> {
                playCommand.execute(event);
            }
            default -> {
            }
        }

    }
}
