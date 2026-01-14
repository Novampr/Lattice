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

public class PlayerEvents {
    public static void onPlayerChat(PlayerChatEvent event) {
        PlayerRef sender = event.getSender();
        String content = event.getContent();

        String webhook = LatticePlugin.config.get().webhook_id;

        LatticePlugin.client
                .withGateway((gateway) -> {
                    return gateway.getWebhookById(Snowflake.of(webhook))
                            .flatMap(hook -> {
                                try {
                                    return hook.execute().withUsername(sender.getUsername()).withContent(content);
                                } catch (Exception e) {
                                    LatticePlugin.LOGGER.atSevere().log("Error while executing webhook", e);
                                    return Mono.error(e);
                                }
                            }).then();
                }).subscribe();
    }

    public static void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();

        LatticePlugin.client.withGateway((gateway) -> gateway
                .getChannelById(Snowflake
                        .of(LatticePlugin.config.get().channel_id)
                )
                .flatMap(ch -> ((TextChannel) ch)
                        .createMessage(player.getDisplayName() + " joined."))
                )
                .subscribe();
    }

    public static void onPlayerDisconnect(PlayerDisconnectEvent event) {
        PlayerRef player = event.getPlayerRef();

        LatticePlugin.client.withGateway((gateway) -> gateway
                        .getChannelById(Snowflake
                                .of(LatticePlugin.config.get().channel_id)
                        )
                        .flatMap(ch -> ((TextChannel) ch)
                                .createMessage(player.getUsername() + " left."))
                )
                .subscribe();
    }
}
