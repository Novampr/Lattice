package me.theclashfruit.lattice.util;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class LatticeConfig {
    public static final BuilderCodec<LatticeConfig> CODEC = BuilderCodec.builder(LatticeConfig.class, LatticeConfig::new)
            .append(
                    new KeyedCodec<>("Enabled", BuilderCodec.BOOLEAN),
                    (config, value, info) -> config.enabled = value,
                    (config, info) -> config.enabled
            )
            .add()
            .append(
                    new KeyedCodec<>("Discord", DiscordConfig.CODEC),
                    (config, value, info) -> config.discord = value,
                    (config, info) -> config.discord
            )
            .add()
            .append(
                    new KeyedCodec<>("ChatPrefix", BuilderCodec.STRING),
                    (config, value, info) -> config.chat_prefix = value,
                    (config, info) -> config.chat_prefix
            )
            .add()
            .append(
                    new KeyedCodec<>("ChatPrefixColour", BuilderCodec. STRING),
                    (config, value, info) -> config.chat_prefix_colour = value,
                    (config, info) -> config.chat_prefix_colour
            )
            .add()
            .build();

    public LatticeConfig() {}

    @Deprecated
    public boolean enabled = false;

    public String chat_prefix = "[Discord]";
    public String chat_prefix_colour = "#5865F2";

    public DiscordConfig discord = new DiscordConfig();

    public static class DiscordConfig {
        public static final BuilderCodec<DiscordConfig> CODEC = BuilderCodec.builder(DiscordConfig.class, DiscordConfig::new)
                .append(
                        new KeyedCodec<>("Token", BuilderCodec.STRING),
                        (config, value, info) -> config.token = value,
                        (config, info) -> config.token
                )
                .add()
                .append(
                        new KeyedCodec<>("GuildId", BuilderCodec.STRING),
                        (config, value, info) -> config.guild_id = value,
                        (config, info) -> config.guild_id
                )
                .add()
                .append(
                        new KeyedCodec<>("ChannelId", BuilderCodec.STRING),
                        (config, value, info) -> config.channel_id = value,
                        (config, info) -> config.channel_id
                )
                .add()
                .append(
                        new KeyedCodec<>("WebhookId", BuilderCodec. STRING),
                        (config, value, info) -> config.webhook_id = value,
                        (config, info) -> config.webhook_id
                )
                .add()
                .append(
                        new KeyedCodec<>("Messages", MessagesConfig.CODEC),
                        (config, value, info) -> config.messages = value,
                        (config, info) -> config.messages
                )
                .add()
                .build();

        public DiscordConfig() {}

        public String token = "your_token_here";

        public String guild_id = "";
        public String channel_id = "";
        public String webhook_id = "";

        public MessagesConfig messages = new MessagesConfig();

        public static class MessagesConfig {
            public static final BuilderCodec<MessagesConfig> CODEC = BuilderCodec.builder(MessagesConfig.class, MessagesConfig::new)
                    .append(
                            new KeyedCodec<>("Join", BuilderCodec.STRING),
                            (config, value, info) -> config.join = value,
                            (config, info) -> config.join
                    )
                    .add()
                    .append(
                            new KeyedCodec<>("Leave", BuilderCodec.STRING),
                            (config, value, info) -> config.leave = value,
                            (config, info) -> config.leave
                    )
                    .add()
                    .build();

            public String join = "%s joined %s.";
            public String leave = "%s left.";
        }
    }
}
