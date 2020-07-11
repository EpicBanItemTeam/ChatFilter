package io.github.euonmyoji.chatfilter.configuration;

import java.util.HashMap;
import java.util.regex.Matcher;

/**
 * @author yinyangshi
 */
@FunctionalInterface
public interface CancelCondition {
    HashMap<String, CancelCondition> CANCEL_CONDITIONS = new HashMap<String, CancelCondition>() {{
        put("any", Matcher::find);
        put("entire", Matcher::matches);
    }};

    static CancelCondition getCancelCondition(String name) {
        return CANCEL_CONDITIONS.getOrDefault(name.toLowerCase(), matcher -> false);
    }

    /**
     * 给定情况的matcher是否需要取消发送消息
     *
     * @param matcher the matcher
     * @return true if cancel
     */
    boolean shouldCancel(Matcher matcher);
}
