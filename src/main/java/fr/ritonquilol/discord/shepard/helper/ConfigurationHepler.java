package fr.ritonquilol.discord.shepard.helper;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:shepard.properties")
@Data
public class ConfigurationHepler {

    @Value("${bot.token}")
    private String token;

    @Value("${bot.guild.id}")
    private String guildId;

    @Value("${bot.client.id}")
    private String clientId;

    @Value("${bot.activity}")
    private String activity;

    @Value("${bot.activity.type}")
    private String activityType;

    @Value("${bot.name}")
    private String botName;
}
