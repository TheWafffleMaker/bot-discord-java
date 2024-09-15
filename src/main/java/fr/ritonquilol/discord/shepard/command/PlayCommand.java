package fr.ritonquilol.discord.shepard.command;

import fr.ritonquilol.discord.shepard.helper.ConfigurationHepler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;
import java.util.Objects;

@Component
public class PlayCommand {

    final ConfigurationHepler configurationHepler;

    public PlayCommand(ConfigurationHepler configurationHepler) {
        this.configurationHepler = configurationHepler;
    }

    public void execute(SlashCommandInteractionEvent event) {
        if (Objects.isNull(event.getMember())) {
            event.reply("Mais qui êtes-vous ?").queue();
        } else if (!isInJukeboxChannel(event)) {
            event.reply("Pour inviter le DJ ça se passe dans #la-sono boudiou !").queue();
        } else if (!isInVoiceChannel(event)) {
            event.reply("S'agirait de faire partie de la fête...").queue();
        } else {
            event.reply("Allez on s'ambiance sur " + event.getInteraction().getOption("query").getAsString() + " dans " + event.getMember().getVoiceState().getChannel().getName()).queue();
        }
    }

    private boolean isInJukeboxChannel(SlashCommandInteractionEvent event) {
        return StringUtils.isNotEmpty(configurationHepler.getJukeboxChannelId())
                && configurationHepler.getJukeboxChannelId().equals(event.getChannelId());
    }

    private boolean isInVoiceChannel(SlashCommandInteractionEvent event) {
        return Objects.nonNull(event.getMember().getVoiceState().getChannel());
    }

}
