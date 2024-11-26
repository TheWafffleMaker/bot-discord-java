package fr.ritonquilol.discord.shepard.listener;

import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.FilterBuilder;
import dev.arbjerg.lavalink.protocol.v4.Karaoke;
import fr.ritonquilol.discord.shepard.manager.GuildMusicManager;
import fr.ritonquilol.discord.shepard.service.AudioLoader;
import fr.ritonquilol.discord.shepard.service.MyUserData;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JDAListener extends ListenerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(JDAListener.class);

    public final Map<Long, GuildMusicManager> musicManagers = new HashMap<>();
    private final LavalinkClient client;

    public JDAListener(LavalinkClient client) {
        this.client = client;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        LOG.info("{} is ready!", event.getJDA().getSelfUser().getAsTag());

        event.getJDA().updateCommands()
                .addCommands(
                        Commands.slash("lyrics", "Testing custom requests"),
                        Commands.slash("node", "What node am I on?"),
                        Commands.slash("eval", "test out some code")
                                .addOption(
                                        OptionType.STRING,
                                        "script",
                                        "Script to eval",
                                        true
                                ),
                        Commands.slash("join", "Join the voice channel you are in."),
                        Commands.slash("leave", "Leaves the vc"),
                        Commands.slash("stop", "Stops the current track"),
                        Commands.slash("pause", "Pause or unpause the player"),
                        Commands.slash("now-playing", "Shows what is currently playing"),
                        Commands.slash("play", "Play a song")
                                .addOption(
                                        OptionType.STRING,
                                        "identifier",
                                        "The identifier of the song you want to play",
                                        true
                                ),
                        Commands.slash("play-file", "Play a song from a file")
                                .addOption(
                                        OptionType.ATTACHMENT,
                                        "file",
                                        "the file to play",
                                        true
                                ),
                        Commands.slash("karaoke", "Turn karaoke on or off")
                                .addSubcommands(
                                        new SubcommandData("on", "Turn karaoke on"),
                                        new SubcommandData("off", "Turn karaoke on")
                                )
                )
                .queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();

        if (guild == null) {
            return;
        }

        switch (event.getFullCommandName()) {
            case "node": {
                final var link = this.client.getLinkIfCached(guild.getIdLong());

                if (link == null) {
                    event.reply("No link for this guild").queue();
                    break;
                }

                event.reply("Connected to: " + link.getNode().getName()).queue();

                break;
            }
            case "join":
                joinHelper(event);
                break;
            case "stop":
                event.reply("Stopped the current track and clearing the queue").queue();
                this.getOrCreateMusicManager(event.getGuild().getIdLong()).stop();
                break;
            case "leave":
                event.getJDA().getDirectAudioController().disconnect(guild);
                event.reply("Leaving your channel!").queue();
                break;
            case "now-playing": {
                final var link = this.client.getOrCreateLink(guild.getIdLong());
                final var player = link.getCachedPlayer();

                if (player == null) {
                    event.reply("Not connected or no player available!").queue();
                    break;
                }

                final var track = player.getTrack();

                if (track == null) {
                    event.reply("Nothing playing currently!").queue();
                    break;
                }

                final var trackInfo = track.getInfo();

                event.reply(
                        "Currently playing: %s%nDuration: %s/%s%nRequester: <@%s>".formatted(
                                trackInfo.getTitle(),
                                player.getPosition(),
                                trackInfo.getLength(),
                                track.getUserData(MyUserData.class).requester()
                        )
                ).queue();
                break;
            }
            case "pause":
                this.client.getOrCreateLink(guild.getIdLong())
                        .getPlayer()
                        .flatMap(player -> player.setPaused(!player.getPaused()))
                        .subscribe(player ->
                                event.reply("Player has been " + (player.getPaused() ? "paused" : "resumed") + "!").queue()
                        );
                break;
            case "karaoke on": {
                final long guildId = guild.getIdLong();
                final Link link = this.client.getOrCreateLink(guildId);

                link.createOrUpdatePlayer()
                        .setFilters(
                                new FilterBuilder()
                                        .setKaraoke(
                                                new Karaoke()
                                        )
                                        .build()
                        )
                        .subscribe();
                event.reply("turning karaoke on!").queue();
                break;
            }
            case "karaoke off": {
                final long guildId = guild.getIdLong();
                final Link link = this.client.getOrCreateLink(guildId);

                link.createOrUpdatePlayer()
                        .setFilters(
                                new FilterBuilder()
                                        .setKaraoke(null)
                                        .build()
                        )
                        .subscribe();
                event.reply("turning karaoke off!").queue();
                break;
            }
            case "play": {
                play(event, guild);

                break;
            }
            case "play-file": {
                // We are already connected, go ahead and play
                if (Objects.requireNonNull(guild.getSelfMember().getVoiceState()).inAudioChannel()) {
                    event.deferReply(false).queue();
                } else {
                    // Connect to VC first
                    joinHelper(event);
                }

                final var file = Objects.requireNonNull(event.getOption("file")).getAsAttachment();
                final long guildId = guild.getIdLong();
                final Link link = this.client.getOrCreateLink(guildId);
                final var mngr = this.getOrCreateMusicManager(guildId);

                link.loadItem(file.getUrl()).subscribe(new AudioLoader(event, mngr));

                break;
            }
            default:
                event.reply("Unknown command???").queue();
                break;
        }
    }

    private void play(@NotNull SlashCommandInteractionEvent event, Guild guild) {
        // We are already connected, go ahead and play
        if (Objects.requireNonNull(guild.getSelfMember().getVoiceState()).inAudioChannel()) {
            event.deferReply(false).queue();
        } else {
            // Connect to VC first
            joinHelper(event);
        }

        final String identifier = Objects.requireNonNull(event.getOption("query")).getAsString();
        LOG.info("id : {}", identifier);
        final long guildId = guild.getIdLong();
        final Link link = this.client.getOrCreateLink(guildId);
        final var mngr = this.getOrCreateMusicManager(guildId);

        link.loadItem(identifier).subscribe(new AudioLoader(event, mngr));
    }

    private GuildMusicManager getOrCreateMusicManager(long guildId) {
        synchronized (this) {
            return this.musicManagers.computeIfAbsent(guildId, id -> new GuildMusicManager(id, this.client));
        }
    }

    // Makes sure that the bot is in a voice channel!
    private void joinHelper(SlashCommandInteractionEvent event) {
        final Member member = event.getMember();
        if (member != null) {
            final GuildVoiceState memberVoiceState = member.getVoiceState();

            if (memberVoiceState != null && memberVoiceState.inAudioChannel()) {
                event.getJDA().getDirectAudioController().connect(memberVoiceState.getChannel());
            }

            this.getOrCreateMusicManager(member.getGuild().getIdLong());

            event.reply("Joining your channel!").queue();
        }
    }
}