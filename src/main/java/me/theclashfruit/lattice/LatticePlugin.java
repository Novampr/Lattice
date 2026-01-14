package me.theclashfruit.lattice;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.Config;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import me.theclashfruit.lattice.events.PlayerEvents;
import me.theclashfruit.lattice.util.LatticeConfig;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LatticePlugin extends JavaPlugin {
    public static DiscordClient client;

    public static Config<LatticeConfig> config;

    public static HytaleLogger LOGGER;

    public LatticePlugin(@Nonnull JavaPluginInit init) {
        super(init);

        LOGGER = this.getLogger();
        config = this.withConfig("Lattice", LatticeConfig.CODEC);

    }

    @Override
    protected void setup() {
        super.setup();
        config.save();

        // do not start the bot if it's disabled
        if (!config.get().enabled) return;

        this.getEventRegistry().registerGlobal(PlayerChatEvent.class, PlayerEvents::onPlayerChat);
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, PlayerEvents::onPlayerReady);
        this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, PlayerEvents::onPlayerDisconnect);

        client = DiscordClient.create(config.get().discord.token);
        client.withGateway(gateway -> {
            gateway.getEventDispatcher()
                    .on(ReadyEvent.class)
                    .subscribe(e ->
                            LOGGER.atInfo().log(
                                    "Logged in as %s#%s",
                                    e.getSelf().getUsername(),
                                    e.getSelf().getDiscriminator()
                            )
                    );

            gateway.getEventDispatcher()
                    .on(MessageCreateEvent.class)
                    .flatMap(event -> {
                        Message msg = event.getMessage();

                        if (msg.getAuthor().map(User::isBot).orElse(true)) {
                            return Mono.empty();
                        }

                        return msg.getChannel()
                                .filter(ch -> ch.getId().equals(Snowflake.of(config.get().discord.channel_id)))
                                .flatMap(ch ->
                                        msg.getAuthorAsMember().doOnNext(member -> {
                                            LOGGER.atInfo().log(
                                                    "%s %s: %s",
                                                    config.get().chat_prefix,
                                                    member.getDisplayName(),
                                                    msg.getContent()
                                            );

                                            List<Attachment> attachments = msg.getAttachments();
                                            Map<String, String> map = attachments.stream()
                                                            .collect(Collectors.toMap(
                                                                    a -> "[" + a.getFilename() + "]",
                                                                    Attachment::getUrl
                                                            ));

                                            StringBuilder builder = new StringBuilder();

                                            if (!msg.getContent().isEmpty()) {
                                                builder.append(" ");
                                                builder.append(msg.getContent());
                                            }

                                            List<com.hypixel.hytale.server.core.Message> msgs =
                                                    attachments.stream()
                                                            .map(a ->
                                                                    com.hypixel.hytale.server.core.Message.join(
                                                                        com.hypixel.hytale.server.core.Message
                                                                                .raw("[" + a.getFilename() + "]")
                                                                                .color(Color.CYAN)
                                                                                .link(a.getUrl()),
                                                                        com.hypixel.hytale.server.core.Message.raw(" ")
                                                                    )
                                                            )
                                                            .toList();

                                            var named = com.hypixel.hytale.server.core.Message.join(
                                                    com.hypixel.hytale.server.core.Message.raw(" "),
                                                    com.hypixel.hytale.server.core.Message.join(msgs.toArray(new com.hypixel.hytale.server.core.Message[0]))
                                            );

                                            var prefix =
                                                    com.hypixel.hytale.server.core.Message
                                                            .raw(config.get().chat_prefix)
                                                            .bold(true)
                                                            .color(config.get().chat_prefix_colour);

                                            var joined = com.hypixel.hytale.server.core.Message.join(
                                                    prefix,
                                                    com.hypixel.hytale.server.core.Message.raw(" "),
                                                    com.hypixel.hytale.server.core.Message.raw(member.getDisplayName()),
                                                    com.hypixel.hytale.server.core.Message.raw(":"),
                                                    com.hypixel.hytale.server.core.Message.raw(builder.toString()),
                                                    named
                                            );

                                            Universe.get().sendMessage(joined);
                                        })
                                );
                    }).subscribe();

            return Mono.never();
        }).subscribe();
    }

    @Override
    protected void shutdown() {
        super.shutdown();

        client.withGateway(GatewayDiscordClient::logout).subscribe();
    }
}
