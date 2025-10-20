package nus.edu.u.services.template.email;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nus.edu.u.domain.dto.common.RenderedTemplateDTO;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/** Renders email templates using Thymeleaf. Handles only email templates (no channel switching). */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final TemplateEngine templateEngine;

    @Override
    public RenderedTemplateDTO render(
            String templateKey, Map<String, Object> variables, Locale locale) {
        Locale effectiveLocale = (locale != null) ? locale : Locale.ENGLISH;
        Context context = new Context(effectiveLocale);

        if (variables != null) {
            variables.forEach(context::setVariable);
        }

        // Template path: src/main/resources/templates/email/<templateKey>.html
        String templatePath = "email/" + templateKey;
        String html;

        try {
            html = templateEngine.process(templatePath, context);
        } catch (Exception e) {
            log.error("Failed to render email template '{}': {}", templatePath, e.getMessage());
            throw new IllegalArgumentException(
                    "Missing or invalid email template: " + templatePath, e);
        }

        String subject = getString(variables, "subject", "Notification");
        String text = getString(variables, "text", null); // optional plain text fallback

        return RenderedTemplateDTO.builder()
                .subject(subject)
                .html(html)
                .text(text)
                .attachments(List.of()) // optional: can be enriched later
                .build();
    }

    // --- Helpers ---
    private String getString(Map<String, Object> vars, String key, String def) {
        if (vars == null) return def;
        Object value = vars.get(key);
        return value == null ? def : String.valueOf(value);
    }
}
