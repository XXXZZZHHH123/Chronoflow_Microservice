package nus.edu.u.services.template.push;

import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.domain.dto.common.RenderedTemplateDTO;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Renders push notification templates using Thymeleaf (optional .txt files). If the template file
 * is missing, it falls back to text from variables["body"].
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PushTemplateServiceImpl implements PushTemplateService {

    private final TemplateEngine templateEngine;

    @Override
    public RenderedTemplateDTO render(
            String templateKey, Map<String, Object> variables, Locale locale) {
        Locale effectiveLocale = (locale != null) ? locale : Locale.ENGLISH;
        Context context = new Context(effectiveLocale);
        if (variables != null) variables.forEach(context::setVariable);

        String templatePath = "push/" + templateKey;
        String body = processOptional(templatePath, context);

        // fallback: if .txt not found or failed, try variable["body"]
        if (body == null && variables != null && variables.containsKey("body")) {
            body = String.valueOf(variables.get("body"));
        }

        String title = getString(variables, "title", "Notification");

        return RenderedTemplateDTO.builder()
                .title(title)
                .body(body)
                .extras(getExtras(variables))
                .build();
    }

    // --- helpers ---

    private String processOptional(String path, Context ctx) {
        try {
            return templateEngine.process(path, ctx);
        } catch (Exception e) {
            log.debug("Optional push template not found or failed to render: {}", path);
            return null;
        }
    }

    private String getString(Map<String, Object> vars, String key, String def) {
        if (vars == null) return def;
        Object v = vars.get(key);
        return v == null ? def : String.valueOf(v);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getExtras(Map<String, Object> vars) {
        if (vars == null) return Map.of();
        Object v = vars.get("extras");
        return (v instanceof Map<?, ?> m) ? (Map<String, Object>) m : Map.of();
    }
}
