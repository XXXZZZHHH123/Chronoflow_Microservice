package nus.edu.u.domain.dto.template;

import java.util.Locale;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import nus.edu.u.enums.template.TemplateProvider;

@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class TemplateRequestDTO {
    private String templateId;
    private Map<String, Object> variables;
    private Locale locale;
    private TemplateProvider templateProvider;
}
