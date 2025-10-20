package nus.edu.u.services.template.ws;


import nus.edu.u.domain.dto.common.RenderedTemplateDTO;

import java.util.Locale;
import java.util.Map;

public interface WsTemplateService {
    RenderedTemplateDTO render(String templateKey, Map<String, Object> variables, Locale locale);
}