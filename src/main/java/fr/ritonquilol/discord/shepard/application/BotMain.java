package fr.ritonquilol.discord.shepard.application;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.LavalinkNode;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.event.*;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import fr.ritonquilol.discord.shepard.command.ShepardCommands;
import fr.ritonquilol.discord.shepard.helper.ConfigurationHepler;
import fr.ritonquilol.discord.shepard.listener.JDAListener;
import fr.ritonquilol.discord.shepard.listener.ShepardEventsListener;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class BotMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(BotMain.class);
    private final ConfigurationHepler configurationHelper;
    private final ShepardEventsListener shepardEventsListener;
    private JDAListener shepardListener;
    private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private JDA api;

    public BotMain(ConfigurationHepler configurationHelper, ShepardEventsListener shepardEventsListener) {
        this.configurationHelper = configurationHelper;
        this.shepardEventsListener = shepardEventsListener;
    }

    @PostConstruct
    public void init() {
        LOGGER.info("------------ Starting {} ------------", configurationHelper.getBotName());
        // Setting up Lavaplayer
        AudioSourceManagers.registerRemoteSources(playerManager);

        LavalinkClient lavalinkClient = new LavalinkClient(Helpers.getUserIdFromToken(configurationHelper.getToken()));
        registerLavalinkNodes(lavalinkClient);
        registerLavalinkListeners(lavalinkClient);
        shepardListener = new JDAListener(lavalinkClient);

        // Setting up Java Discord API connection
        api = JDABuilder.createDefault(configurationHelper.getToken())
                .setVoiceDispatchInterceptor(new JDAVoiceUpdateListener(lavalinkClient))
                .addEventListeners(shepardListener)
                .addEventListeners(shepardEventsListener)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES)
                .build();

        // Loading commands
        api.upsertCommand(ShepardCommands.ZOUI_COMMAND).complete();

    }

    private void registerLavalinkNodes(LavalinkClient client) {
        List.of(
                client.addNode(
                        new NodeOptions.Builder()
                                .setName("link")
                                .setServerUri("ws://localhost:2333")
                                .setPassword("saucisse123.")
                                .build())
        ).forEach(node -> node.on(TrackStartEvent.class).subscribe(event -> {
            final LavalinkNode node1 = event.getNode();

            LOGGER.trace(
                    "{}: track started: {}",
                    node1.getName(),
                    event.getTrack().getInfo()
            );
        }));
    }

    private void registerLavalinkListeners(LavalinkClient client) {
        client.on(ReadyEvent.class).subscribe(event -> {
            final LavalinkNode node = event.getNode();

            LOGGER.info(
                    "Node '{}' is ready, session id is '{}'!",
                    node.getName(),
                    event.getSessionId()
            );
        });

        client.on(StatsEvent.class).subscribe(event -> {
            final LavalinkNode node = event.getNode();

            LOGGER.info(
                    "Node '{}' has stats, current players: {}/{} (link count {})",
                    node.getName(),
                    event.getPlayingPlayers(),
                    event.getPlayers(),
                    client.getLinks().size()
            );
        });

        client.on(TrackStartEvent.class).subscribe(event ->
                Optional.ofNullable(shepardListener.musicManagers.get(event.getGuildId())).ifPresent(
                        mng -> mng.scheduler.onTrackStart(event.getTrack())
                )
        );

        client.on(TrackEndEvent.class).subscribe(event ->
                Optional.ofNullable(shepardListener.musicManagers.get(event.getGuildId())).ifPresent(
                        mng -> mng.scheduler.onTrackEnd(api, event, event.getEndReason())
                )
        );

        client.on(EmittedEvent.class).subscribe(event -> {
            if (event instanceof TrackStartEvent) {
                LOGGER.info("Is a track start event!");
            }

            final var node = event.getNode();

            LOGGER.info(
                    "Node '{}' emitted event: {}",
                    node.getName(),
                    event
            );
        });
    }
}
