package nus.edu.u.services.template.push;



import nus.edu.u.domain.dto.common.RenderedTemplateDTO;

import java.util.Locale;
import java.util.Map;

public interface PushTemplateService {
    RenderedTemplateDTO render(String templateKey, Map<String, Object> variables, Locale locale);
}
