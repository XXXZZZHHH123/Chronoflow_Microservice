package nus.edu.u.services.template.ws;

import java.util.Locale;
import java.util.Map;
import nus.edu.u.domain.dto.common.RenderedTemplateDTO;

public interface WsTemplateService {
    RenderedTemplateDTO render(String templateKey, Map<String, Object> variables, Locale locale);
}
