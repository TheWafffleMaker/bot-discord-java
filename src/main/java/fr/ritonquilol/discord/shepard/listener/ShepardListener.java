package fr.ritonquilol.discord.shepard.listener;

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
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        LOGGER.info("Slash command interaction : {}", event.getName());
    }
}
