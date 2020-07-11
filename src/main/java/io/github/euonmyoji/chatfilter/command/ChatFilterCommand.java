package io.github.euonmyoji.chatfilter.command;

import io.github.euonmyoji.chatfilter.configuration.ChatFilterData;
import io.github.euonmyoji.chatfilter.configuration.PluginConfig;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.spongepowered.api.text.Text.of;

/**
 * @author yinyangshi
 */
public class ChatFilterCommand {
    private static final CommandSpec TEST = CommandSpec.builder()
            .permission("chatfilter.command.admin.test")
            .arguments(GenericArguments.string(of("pattern")), GenericArguments.string(of("text")))
            .executor((src, args) -> {
                String patternStr = args.<String>getOne(of("pattern")).orElseThrow(IllegalArgumentException::new);
                String text = args.<String>getOne(of("text")).orElseThrow(IllegalArgumentException::new);
                Matcher matcher = Pattern.compile(patternStr).matcher(text);
                int last = 0;
                Text.Builder builder = Text.builder();
                while (matcher.find(last)) {
                    builder.append(of(TextColors.WHITE, text.substring(last, matcher.start())))
                            .append(of(TextColors.RED, text.substring(matcher.start(), matcher.end())));
                    last = matcher.end();
                }
                src.sendMessage(builder.append(of(text.substring(last))).build());
                return CommandResult.success();
            })
            .build();
    private static final CommandSpec LIST = CommandSpec.builder()
            .permission("chatfilter.command.admin.list")
            .arguments(GenericArguments.optional(GenericArguments.string(of("key"))))
            .executor((src, args) -> {
                Collection<String> keys = args.getAll("key");
                PaginationList.Builder builder = PaginationList.builder()
                        .padding(of("-"))
                        .title(of("Filters"));
                if (keys.size() == 0) {
                    builder.contents(PluginConfig.filters.values().stream().map(chatFilterData ->
                            of(chatFilterData.key + "(" + chatFilterData.weight + "): " + chatFilterData.pattern)
                                    .toBuilder()
                                    .onClick(TextActions.runCommand("/cf list " + chatFilterData.key))
                                    .build()).collect(Collectors.toList()));
                } else {
                    builder.contents(keys.stream().map(s -> {
                        ChatFilterData data = PluginConfig.filters.get(s);
                        if (data == null) {
                            return of(s + "(?): ?");
                        } else {
                            return of(data.key + "(" + data.weight + "): " + data.pattern
                                            + "\n    cancelMessage:", data.cancelMessage,
                                    "\n    enable-replacement:" + data.enableReplacement
                                            + "\n    replacement:" + data.replacementList);
                        }
                    }).collect(Collectors.toList()));
                }
                builder.build().sendTo(src);
                return CommandResult.success();
            })
            .build();


    public static final CommandSpec CHAT_FILTER = CommandSpec.builder()
            .child(TEST, "test")
            .child(LIST, "list", "info")
            .build();
}
