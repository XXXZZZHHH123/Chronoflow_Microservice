package nus.edu.u.services.template.email;

import java.util.Locale;
import java.util.Map;
import nus.edu.u.domain.dto.common.RenderedTemplateDTO;

public interface EmailTemplateService {
    RenderedTemplateDTO render(String templateKey, Map<String, Object> variables, Locale locale);
}
