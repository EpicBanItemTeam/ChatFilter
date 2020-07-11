package io.github.euonmyoji.chatfilter.configuration;

import io.github.euonmyoji.chatfilter.ChatFilter;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Objects;

import static io.github.euonmyoji.chatfilter.ChatFilter.logger;

/**
 * @author yinyangshi
 */
public class PluginConfig {
    public static final String PERMISSION_NODE_PATTERN = "[_\\-0-9a-zA-Z]+";
    public static final LinkedHashMap<String, ChatFilterData> filters = new LinkedHashMap<>();
    public static boolean showRawMsg = true;
    private static HoconConfigurationLoader loader;
    private static CommentedConfigurationNode root;

    static {
        loader = HoconConfigurationLoader.builder().setPath(ChatFilter.plugin.defaultCfgDir.resolve("global.conf")).build();
    }

    public static void reload() {
        try {
            root = loader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
            CommentedConfigurationNode filtersNode = root.getNode("filters");
            showRawMsg = root.getNode("settings", "showRawMsg").getBoolean(true);
            if (Files.notExists(ChatFilter.plugin.defaultCfgDir.resolve("global.conf"))) {
                filtersNode.getNode("ljyys", "pattern").setValue("l.*j.*yys");
                filtersNode.getNode("ljyys", "weight").setValue(0);
                filtersNode.getNode("ljyys", "enable-replacement").setValue(true);
                filtersNode.getNode("ljyys", "replacement").setValue("ljyys");
                filtersNode.getNode("ljyys", "cancel-condition").setValue("");
                filtersNode.getNode("ljyys", "cancelMessage").setValue("");
                loader.save(root);
            }
            filters.clear();
            filtersNode.getChildrenMap().entrySet().stream().map(objectEntry -> {
                String key = objectEntry.getKey().toString();
                if (key.matches(PERMISSION_NODE_PATTERN)) {
                    try {
                        return new ChatFilterData(key, objectEntry.getValue());
                    } catch (Throwable throwable) {
                        logger.warn("load filter failed:" + key, throwable);
                    }
                }
                return null;
            }).filter(Objects::nonNull).sorted()
                    .forEach(chatFilterData -> filters.put(chatFilterData.key, chatFilterData));
        } catch (IOException e) {
            logger.warn("reload failed", e);
        }
    }
}
