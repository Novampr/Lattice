package me.theclashfruit.lattice.events;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.TextChannel;
import me.theclashfruit.lattice.LatticePlugin;
import reactor.core.publisher.Mono;

import static me.theclashfruit.lattice.LatticePlugin.LOGGER;

public class PlayerEvents {
    public static void onPlayerChat(PlayerChatEvent event) {
        PlayerRef sender = event.getSender();
        String content = event.getContent();

        String webhook = LatticePlugin.config.get().discord.webhook_id;

        LatticePlugin.client
                .withGateway((gateway) -> {
                    return gateway.getWebhookById(Snowflake.of(webhook))
                            .flatMap(hook -> {
                                try {
                                    return hook.execute().withUsername(sender.getUsername()).withContent(content);
                                } catch (Exception e) {
                                    LOGGER.atSevere().log("Error while executing webhook", e);
                                    return Mono.error(e);
                                }
                            }).then();
                }).subscribe();
    }

    public static void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();

        if(player.getWorld() != null) {
            String joinMessage = String.format(LatticePlugin.config.get().discord.messages.join, player.getDisplayName(), player.getWorld().getName());

            LatticePlugin.client.withGateway((gateway) -> gateway
                            .getChannelById(Snowflake
                                    .of(LatticePlugin.config.get().discord.channel_id)
                            )
                            .flatMap(ch -> ((TextChannel) ch)
                                    .createMessage(joinMessage)
                            )
                    ).subscribe();
        } else {
            LOGGER.atWarning().log("Player's world is null.");
        }
    }

    public static void onPlayerDisconnect(PlayerDisconnectEvent event) {
        PlayerRef player = event.getPlayerRef();

        String quitMessage = String.format(LatticePlugin.config.get().discord.messages.leave, player.getUsername());

        LatticePlugin.client.withGateway((gateway) -> gateway
                        .getChannelById(Snowflake
                                .of(LatticePlugin.config.get().discord.channel_id)
                        )
                        .flatMap(ch -> ((TextChannel) ch)
                                .createMessage(quitMessage)
                        )
                ).subscribe();
    }
}
