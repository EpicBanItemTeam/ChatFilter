package io.github.euonmyoji.chatfilter.configuration;

import io.github.euonmyoji.chatfilter.ChatFilter;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.TypeTokens;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * @author yinyangshi
 */
public class ChatFilterData implements Comparable<ChatFilterData> {
    public final List<String> replacementList;
    public final String key;
    public final Pattern pattern;
    public final int weight;
    public final boolean enableReplacement;
    public final Supplier<String> replacement;
    public final CancelCondition cancelCondition;
    public final Text cancelMessage;

    public ChatFilterData(String key, CommentedConfigurationNode filterNode) {
        Supplier<String> replacement1;
        this.key = key;
        pattern = Pattern.compile(Objects.requireNonNull(filterNode.getNode("pattern").getString()));
        weight = filterNode.getNode("weight").getInt(0);
        enableReplacement = filterNode.getNode("enable-replacement").getBoolean(true);
        List<String> list = Collections.emptyList();
        try {
            list = filterNode.getNode("replacement").getList(TypeTokens.STRING_TOKEN);
            if (list.size() <= 1) {
                String s = list.size() == 0 ? "" : list.get(0);
                replacement1 = () -> s;
            } else {
                Random random = new Random();
                List<String> javaGiveAway = list;
                replacement1 = () -> javaGiveAway.get(random.nextInt(javaGiveAway.size()));
            }
        } catch (ObjectMappingException e) {
            replacement1 = () -> "";
            ChatFilter.logger.info("load replacement failed", e);
        }
        this.replacementList = list;
        replacement = replacement1;
        cancelCondition = CancelCondition.getCancelCondition(filterNode.getNode("cancel-condition").getString(""));
        cancelMessage = TextSerializers.FORMATTING_CODE.deserialize(filterNode.getNode("cancelMessage").getString(""));
    }

    @Override
    public int compareTo(ChatFilterData o) {
        return Integer.compare(o.weight, weight);
    }
}
