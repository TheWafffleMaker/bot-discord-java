package fr.ritonquilol.discord.shepard.application;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import fr.ritonquilol.discord.shepard.command.ShepardCommands;
import fr.ritonquilol.discord.shepard.helper.ConfigurationHepler;
import fr.ritonquilol.discord.shepard.listener.ShepardTrackScheduler;
import fr.ritonquilol.discord.shepard.listener.ShepardEventsListener;
import fr.ritonquilol.discord.shepard.listener.ShepardListener;
import fr.ritonquilol.discord.shepard.service.MusicService;
import jakarta.annotation.PostConstruct;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BotMain {

    private static final Logger LOGGER = LoggerFactory.getLogger(BotMain.class);
    private final ConfigurationHepler configurationHelper;
    private final ShepardEventsListener shepardEventsListener;
    private final ShepardListener shepardListener;
    private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private final MusicService musicService;

    public BotMain(ConfigurationHepler configurationHelper, ShepardEventsListener shepardEventsListener, ShepardListener shepardListener, MusicService musicService) {
        this.configurationHelper = configurationHelper;
        this.shepardEventsListener = shepardEventsListener;
        this.shepardListener = shepardListener;
        this.musicService = musicService;
    }

    @PostConstruct
    public void init() {
        LOGGER.info("------------ Starting {} ------------", configurationHelper.getBotName());
        // Setting up Lavaplayer
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioPlayer player = playerManager.createPlayer();

        ShepardTrackScheduler shepardTrackScheduler = new ShepardTrackScheduler(player);
        player.addListener(shepardTrackScheduler);

        // Setting up Java Discord API connection
        JDA api = JDABuilder.createDefault(configurationHelper.getToken())
                .addEventListeners(shepardListener)
                .addEventListeners(shepardEventsListener)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES)
                .build();

        // Loading commands
        api.upsertCommand(ShepardCommands.ZOUI_COMMAND).complete();

    }
}
