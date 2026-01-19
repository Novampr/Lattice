package me.theclashfruit.lattice.discord;

import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import me.theclashfruit.lattice.LatticePlugin;
import me.theclashfruit.lattice.util.LinkUtil;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

import java.awt.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import static me.theclashfruit.lattice.LatticePlugin.LOGGER;
import static me.theclashfruit.lattice.LatticePlugin.config;

public class BotEventListener extends ListenerAdapter {
    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        var jda = event.getJDA();
        var conf = config.get();

        LOGGER.atInfo().log("Logged in as %s#%s", jda.getSelfUser().getName(), jda.getSelfUser().getDiscriminator());

        // Register slash commands on Discord for guild or global based on if guild_id is set in the config.
        if (conf.discord.guild_id.isEmpty() || !StringUtil.isNumericString(conf.discord.guild_id)) {
            // Register global commands.
            jda.updateCommands().addCommands(
                    Commands.slash("link", "Link your Hytale account with your Discord account.")
                            .addOption(OptionType.STRING, "code", "The code shown in the Hytale chat.", true),
                    Commands.slash("unlink", "Unlink your Discord account from your Hytale account.")
            ).queue();
        } else {
            // Register guild commands.
            try {
                jda.getGuildById(conf.discord.guild_id).updateCommands().addCommands(
                        Commands.slash("link", "Link your Hytale account with your Discord account.")
                                .addOption(OptionType.STRING, "code", "The code shown in the Hytale chat.", true),
                        Commands.slash("unlink", "Unlink your Discord account from your Hytale account.")
                ).queue();
            } catch (Exception e) {
                LOGGER.atWarning().withCause(e).log("Failed to create guild commands, you may have provided an invalid guild id.");
            }
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        var user    = event.getAuthor();
        var channel = event.getChannel();
        var message = event.getMessage();

        var config = LatticePlugin.config.get();

        // TODO: Implement slash commands.
        if(handleCommands(user, message)) return;
        if (!channel.getId().equals(config.discord.channel_id)) return;

        var attachments = message.getAttachments();

        StringBuilder builder = new StringBuilder();

        if (!message.getContentDisplay().isEmpty()) {
            builder.append(" ");
            builder.append(message.getContentDisplay());
        }

        var attachmentMessages = attachments.stream().map(a -> Message.join(Message.raw("[" + a.getFileName() + "]").color(Color.CYAN).link(a.getUrl()), Message.raw(" "))).toList();
        var compiledAttachments = Message.join(
                Message.raw(" "),
                Message.join(attachmentMessages.toArray(new Message[0]))
        );

        var prefix = Message
                .raw(config.chat_prefix)
                .color(config.chat_prefix_colour)
                .bold(true);

        var joined = Message.join(
                prefix,
                Message.raw(" "),
                Message.raw(user.getEffectiveName()),
                Message.raw(":"),
                Message.raw(builder.toString()),
                compiledAttachments
        );

        Universe.get().sendMessage(joined);
        LOGGER.atInfo().log("[Discord] %s:%s %s", user.getEffectiveName(), builder.toString(), String.join(" ", attachments.stream().map(a -> "[" + a.getFileName() + "]").toList()));
    }

    @Deprecated(forRemoval = true)
    private boolean handleCommands(User user, net.dv8tion.jda.api.entities.Message message) {
        try {
            String contents = message.getContentDisplay();
            String[] tokens = contents.split(" ");

            if (tokens[0].equals("-link")) {
                String code = tokens[1];

                PlayerRef ref = LinkUtil.getPlayerFromCode(code);
                if (ref == null)
                    message.reply("Invalid code.").queue();
                else {
                    Map<String, String> connections = LatticePlugin.connections.get().connections;
                    connections.put(ref.getUuid().toString(), user.getId());
                    LatticePlugin.connections.save();

                    LinkUtil.removeCode(code);

                    ref.sendMessage(Message.raw("Successfully linked Discord account.").color(Color.GREEN).bold(true));
                }

                return true;
            }
        } catch (Exception e) {
            message.reply("There was an error executing this command!").queue();
            LOGGER.atWarning().withCause(e).log("Discord command failed!");

            return true;
        }

        return false;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String name = event.getName();

        if (name.equals("link")) {
            String code = Objects.requireNonNull(event.getOption("code")).getAsString();

            PlayerRef ref = LinkUtil.getPlayerFromCode(code);
            if (ref == null)
                event.reply("Invalid code.").setEphemeral(true).queue();
            else {
                Map<String, String> connections = LatticePlugin.connections.get().connections;
                connections.put(ref.getUuid().toString(), event.getUser().getId());
                LatticePlugin.connections.save();

                LinkUtil.removeCode(code);

                event.reply("Successfully linked Hytale account.").setEphemeral(true).queue();
                ref.sendMessage(Message.raw("Successfully linked Discord account.").color(Color.GREEN).bold(true));
            }
        } else if (name.equals("unlink")) {
            Map<String, String> connections = LatticePlugin.connections.get().connections;

            if (connections.containsValue(event.getUser().getId())) {
                for (Map.Entry<String, String> entry : connections.entrySet()) {
                    if (event.getUser().getId().equals(entry.getValue())) {
                        connections.remove(entry.getKey());
                        LatticePlugin.connections.save();
                    }
                }

                event.reply("Successfully unlinked the Hytale account.").setEphemeral(true).queue();
            } else {
                event.reply("You don't have a Hytale account linked.").setEphemeral(true).queue();
            }
        }
    }
}
