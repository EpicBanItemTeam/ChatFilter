package io.github.euonmyoji.chatfilter;

import com.google.inject.Inject;
import io.github.euonmyoji.chatfilter.command.ChatFilterCommand;
import io.github.euonmyoji.chatfilter.configuration.ChatFilterData;
import io.github.euonmyoji.chatfilter.configuration.PluginConfig;
import org.bstats.sponge.Metrics2;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Before;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;

/**
 * @author yinyangshi
 */
@Plugin(id = "chatfilter", name = "ChatFilter",
        version = "@spongeVersion", description = "Filter chat message.", authors = "yinyangshi")
public class ChatFilter {
    public static Logger logger;
    public static ChatFilter plugin;
    @Inject
    @ConfigDir(sharedRoot = false)
    public Path defaultCfgDir;
    @Inject
    private Metrics2 metrics;

    @Inject
    public ChatFilter(Logger logger) {
        ChatFilter.logger = logger;
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        plugin = this;
        try {
            Files.createDirectories(defaultCfgDir);
        } catch (IOException e) {
            logger.warn("Create config directory failed", e);
        }
        PluginConfig.reload();
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        PluginConfig.reload();
    }

    @Listener(beforeModifications = true, order = Order.EARLY)
    public void onChat(MessageChannelEvent.Chat event) {
        //noinspection ConstantConditions (replace)
        if (Sponge.getServer().getOnlineMode() || Boolean.parseBoolean("@OFFLINE@")) {
            event.getCause().first(User.class).ifPresent(user -> {
                boolean changed = false;
                String msg = event.getRawMessage().toPlain();
                for (ChatFilterData value : PluginConfig.filters.values()) {
                    if (!user.hasPermission("chatfilter.bypass." + value.key)) {
                        Matcher matcher = value.pattern.matcher(msg);
                        if (value.cancelCondition.shouldCancel(matcher)) {
                            event.setMessageCancelled(true);
                            if (!value.cancelMessage.isEmpty()) {
                                user.getPlayer().ifPresent(player -> player.sendMessage(value.cancelMessage));
                            }
                            if (PluginConfig.showRawMsg) {
                                logger.info(user.getName() + ": " + event.getRawMessage().toPlain());
                            }
                            return;
                        }
                        if (matcher.find() && value.enableReplacement) {
                            changed = true;
                            msg = matcher.replaceAll(value.replacement.get());
                        }
                    }
                }
                if(changed) {
                    if (PluginConfig.showRawMsg) {
                        logger.info(user.getName() + ": " + event.getRawMessage().toPlain());
                    }
                    event.getFormatter().setBody(Text.of(msg));
                }
            });
        }
    }

    @Listener
    public void onStarted(GameStartedServerEvent event) {
        //noinspection ConstantConditions (replace)
        if (Sponge.getServer().getOnlineMode() || Boolean.parseBoolean("@OFFLINE@")) {
            Sponge.getCommandManager().register(this, ChatFilterCommand.CHAT_FILTER, "chatfilter", "cf");
            onReload(null);
        }
    }

}
