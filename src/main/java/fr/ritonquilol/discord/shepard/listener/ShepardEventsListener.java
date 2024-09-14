package fr.ritonquilol.discord.shepard.listener;

import fr.ritonquilol.discord.shepard.helper.ConfigurationHepler;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ShepardEventsListener implements EventListener {

    private final ConfigurationHepler configurationHelper;
    private static final Logger LOGGER = LoggerFactory.getLogger(ShepardEventsListener.class);

    private ShepardEventsListener(ConfigurationHepler configurationHelper) {
        this.configurationHelper = configurationHelper;
    }
    @Override
    public void onEvent(GenericEvent event) {
        if (event instanceof ReadyEvent) {
            LOGGER.info("------------- {} started -------------", configurationHelper.getBotName());
        }
    }
}
