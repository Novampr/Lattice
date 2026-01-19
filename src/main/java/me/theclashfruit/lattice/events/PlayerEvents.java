package me.theclashfruit.lattice.events;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import me.theclashfruit.lattice.LatticePlugin;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.net.URI;
import java.util.Map;

import static me.theclashfruit.lattice.LatticePlugin.LOGGER;
import static me.theclashfruit.lattice.LatticePlugin.jda;

public class PlayerEvents {
    public static void onPlayerChat(PlayerChatEvent event) {
        PlayerRef sender = event.getSender();
        String content = event.getContent();

        String webhook = LatticePlugin.config.get().discord.webhook_id;
        // check if someone put the whole webhook url and parse it
        try {
            var webhookUrl = new URI(webhook);
            if (webhookUrl.getHost().equals("discord.com")) {
                var segments = webhookUrl.getPath().split("/");
                if (segments.length >= 3)
                    webhook = segments[2];
                else
                    throw new Exception("Invalid webhook url.");

                // replace it with just the id in the config
                LatticePlugin.config.get().discord.webhook_id = webhook;
                LatticePlugin.config.save();
            } else {
                throw new Exception("Webhook is not just the id or a valid discord.com webhook url.");
            }
        } catch (Exception e) {
            LOGGER.atWarning().log(e.getMessage());
        }

        Map<String, String> connections = LatticePlugin.connections.get().connections;

        var hook = jda.retrieveWebhookById(webhook);
        if (connections.containsKey(sender.getUuid().toString())) {
            var user = jda.getUserById(connections.get(sender.getUuid().toString()));
            if (user == null) {
                hook.flatMap(h -> h.sendMessage(content).setUsername(sender.getUsername())).queue();

                return;
            }

            hook.flatMap(h -> h.sendMessage(content).setUsername(user.getEffectiveName()).setAvatarUrl(user.getAvatarUrl())).queue();
        } else {
            hook.flatMap(h -> h.sendMessage(content).setUsername(sender.getUsername())).queue();
        }
    }

    public static void onPlayerReady(PlayerReadyEvent event) {
        Player player = event.getPlayer();

        if(player.getWorld() != null) {
            String joinMessage = String.format(LatticePlugin.config.get().discord.messages.join, player.getDisplayName(), player.getWorld().getName());

            var channel = jda.getChannelById(TextChannel.class, LatticePlugin.config.get().discord.channel_id);
            if (channel != null)
                channel.sendMessage(joinMessage).queue();
            else
                LOGGER.atWarning().log("Channel is null.");
        } else
            LOGGER.atWarning().log("Player's world is null.");
    }

    public static void onPlayerDisconnect(PlayerDisconnectEvent event) {
        PlayerRef player = event.getPlayerRef();

        String quitMessage = String.format(LatticePlugin.config.get().discord.messages.leave, player.getUsername());

        var channel = jda.getChannelById(TextChannel.class, LatticePlugin.config.get().discord.channel_id);
        if (channel != null)
            channel.sendMessage(quitMessage).queue();
        else
            LOGGER.atWarning().log("Channel is null.");
    }
}
