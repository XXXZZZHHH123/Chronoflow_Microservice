package nus.edu.u.services.template.ws;

import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.domain.dto.common.RenderedTemplateDTO;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Renders WS templates from templates/ws/<templateKey>.txt (optional). Falls back to
 * variables["body"] when the file is missing. Extras defaults to variables["extras"] or the whole
 * variables map.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WsTemplateServiceImpl implements WsTemplateService {

    private final TemplateEngine templateEngine;

    @Override
    public RenderedTemplateDTO render(
            String templateKey, Map<String, Object> variables, Locale locale) {
        Locale effectiveLocale = (locale != null) ? locale : Locale.ENGLISH;
        Context ctx = new Context(effectiveLocale);
        if (variables != null) variables.forEach(ctx::setVariable);

        String path = "ws/" + templateKey;
        String body = processOptional(path, ctx);

        // Fallback: if no template file or render failed, try variables["body"]
        if (body == null && variables != null && variables.containsKey("body")) {
            body = String.valueOf(variables.get("body"));
        }

        return RenderedTemplateDTO.builder().body(body).extras(resolveExtras(variables)).build();
    }

    // ---- helpers ----

    private String processOptional(String path, Context ctx) {
        try {
            return templateEngine.process(path, ctx);
        } catch (Exception e) {
            log.debug("Optional WS template not found or failed to render: {}", path);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> resolveExtras(Map<String, Object> vars) {
        if (vars == null) return Map.of();
        Object ex = vars.get("extras");
        if (ex instanceof Map<?, ?> m) return (Map<String, Object>) m;
        // For WS, sending full vars as extras is often handy:
        return vars;
    }
}
