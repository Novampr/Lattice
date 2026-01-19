package me.theclashfruit.lattice.util.store;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

// yeah, don't ask why this is a config file, it just felt easier to do it this way.
public class DiscordDataStore {
    public static final BuilderCodec<DiscordDataStore> CODEC = BuilderCodec.builder(DiscordDataStore.class, DiscordDataStore::new)
            .append(
                    new KeyedCodec<>("_Note", BuilderCodec.STRING),
                    (config, value, info) -> config._note = "DO NOT MODIFY THIS FILE.", // do not ~~redeem~~ modify ma'am
                    (config, info) -> config._note
            )
            .add()
            .append(
                    new KeyedCodec<>("Webhook", BuilderCodec.STRING),
                    (config, value, info) -> config.webhook = value,
                    (config, info) -> config.webhook
            )
            .add()
            .append(
                    new KeyedCodec<>("Connections", new SetCodec<>(Connection.CODEC, java.util.HashSet::new, false)),
                    (config, value, info) -> config.connections = value.stream()
                            .collect(Collectors.toMap(
                                    c -> c.uuid,
                                    c -> c.snowflake
                            )),
                    (config, info) -> config.connections.entrySet().stream()
                            .map(e -> {
                                Connection c = new Connection();
                                c.uuid = e.getKey();
                                c.snowflake = e.getValue();
                                return c;
                            })
                            .collect(Collectors.toSet())
            )
            .add()
            .build();

    public DiscordDataStore() {}

    private String _note = "DO NOT MODIFY THIS FILE.";

    public Map<String, String> connections = new HashMap<>();
    public String webhook = "";

    public static class Connection {
        public static final BuilderCodec<Connection> CODEC = BuilderCodec.builder(Connection.class, Connection::new)
                .append(
                        new KeyedCodec<>("Uuid", BuilderCodec.STRING),
                        (config, value, info) -> config.uuid = value,
                        (config, info) -> config.uuid
                )
                .add()
                .append(
                        new KeyedCodec<>("Snowflake", BuilderCodec.STRING),
                        (config, value, info) -> config.snowflake = value,
                        (config, info) -> config.snowflake
                )
                .add()
                .build();

        public String uuid;
        public String snowflake;

        public Connection() {}
    }
}
