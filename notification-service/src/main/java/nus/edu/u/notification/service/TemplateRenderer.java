package nus.edu.u.notification.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple template renderer used by notification components to expand dynamic placeholders.
 *
 * <p>Placeholders are defined with the syntax <code>{{key}}</code>.
 */
public class TemplateRenderer {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{([a-zA-Z0-9_.-]+)}}");

    /**
     * Render template with provided variables.
     *
     * @param template the template string
     * @param variables key-value pairs used during rendering
     * @return rendered string
     */
    public String render(String template, Map<String, ?> variables) {
        if (StrUtil.isEmpty(template) || CollUtil.isEmpty(variables)) {
            return template;
        }
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = variables.get(key);
            String replacement = value != null ? value.toString() : matcher.group(0);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * Extract all placeholders present in the template.
     *
     * @param template template string
     * @return unique set of placeholder keys
     */
    public Set<String> extractPlaceholders(String template) {
        if (StrUtil.isEmpty(template)) {
            return Collections.emptySet();
        }
        Matcher matcher = PLACEHOLDER.matcher(template);
        Set<String> placeholders = new LinkedHashSet<>();
        while (matcher.find()) {
            placeholders.add(matcher.group(1));
        }
        return placeholders;
    }
}
