package me.theclashfruit.lattice;

import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import me.theclashfruit.lattice.commands.LatticeCommand;
import me.theclashfruit.lattice.discord.BotEventListener;
import me.theclashfruit.lattice.events.PlayerEvents;
import me.theclashfruit.lattice.util.store.DiscordDataStore;
import me.theclashfruit.lattice.util.LatticeConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.annotation.Nonnull;

public class LatticePlugin extends JavaPlugin {
    public static JDA jda;
    public static Config<LatticeConfig> config;

    public static Config<DiscordDataStore> connections;

    public static HytaleLogger LOGGER;

    public LatticePlugin(@Nonnull JavaPluginInit init) {
        super(init);

        LOGGER = this.getLogger();

        config = this.withConfig("Lattice", LatticeConfig.CODEC);
        connections = this.withConfig("DiscordData", DiscordDataStore.CODEC);
    }

    @Override
    protected void setup() {
        super.setup();
        config.save();
        connections.save();

        // Commands
        this.getCommandRegistry().registerCommand(new LatticeCommand());

        var conf = config.get();

        // Do not start unless all info is present in the config.
        // NOTE: `Enabled` is now deprecated and has no effect.
        if (conf.discord.token.isEmpty() || conf.discord.token.equals("your_token_here")) {
            LOGGER.atWarning().log("Please set `Discord.Token` to your token!");
            return;
        }
        if (conf.discord.channel_id.isEmpty() || !StringUtil.isNumericString(conf.discord.channel_id)) {
            LOGGER.atWarning().log("Please set `Discord.ChannelId` to your channel's id!");
            return;
        }
        if (conf.discord.webhook_id.isEmpty()) {
            LOGGER.atWarning().log("Please set `Discord.WebhookId` to your webhooks's id or full url!");
            return;
        }

        jda = JDABuilder
                .createDefault(config.get().discord.token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .setEventPassthrough(true)
                .addEventListeners(new BotEventListener())
                .build();

        this.getEventRegistry().registerGlobal(PlayerChatEvent.class, PlayerEvents::onPlayerChat);
        this.getEventRegistry().registerGlobal(PlayerReadyEvent.class, PlayerEvents::onPlayerReady);
        this.getEventRegistry().registerGlobal(PlayerDisconnectEvent.class, PlayerEvents::onPlayerDisconnect);
    }

    @Override
    protected void shutdown() {
        super.shutdown();

        jda.shutdown();
    }
}
