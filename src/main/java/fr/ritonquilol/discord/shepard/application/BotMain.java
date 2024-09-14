package fr.ritonquilol.discord.shepard.application;

import fr.ritonquilol.discord.shepard.helper.ConfigurationHepler;
import fr.ritonquilol.discord.shepard.listener.ShepardEventsListener;
import fr.ritonquilol.discord.shepard.listener.ShepardListener;
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

    public BotMain(ConfigurationHepler configurationHelper, ShepardEventsListener shepardEventsListener, ShepardListener shepardListener) {
        this.configurationHelper = configurationHelper;
        this.shepardEventsListener = shepardEventsListener;
        this.shepardListener = shepardListener;
    }

    @PostConstruct
    public void init() {
        LOGGER.info("------------ Starting {} ------------", configurationHelper.getBotName());
        JDA api = JDABuilder.createDefault(configurationHelper.getToken())
                .addEventListeners(shepardListener)
                .addEventListeners(shepardEventsListener)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES)
                .build();
    }
}
