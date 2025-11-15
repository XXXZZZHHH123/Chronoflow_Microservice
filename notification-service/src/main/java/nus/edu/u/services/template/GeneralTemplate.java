package nus.edu.u.services.template;

import nus.edu.u.domain.dto.template.TemplateRequestDTO;
import nus.edu.u.domain.dto.template.TemplateResponseDTO;
import nus.edu.u.provider.template.TemplateClientFactory;
import nus.edu.u.services.notification.TemplateEngineImplementor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

@Component("generalTemplate")
public class GeneralTemplate implements TemplateEngineImplementor {

    private final TemplateClientFactory templateClientFactory = new TemplateClientFactory();

    @Override
    public TemplateResponseDTO process(TemplateRequestDTO templateRequestDTO) {
        var variables = templateRequestDTO.getVariables();
        var templateProvider = templateRequestDTO.getTemplateProvider();
        var templateEngine = templateClientFactory.getClient(templateProvider);

        Context context = new Context(templateRequestDTO.getLocale());
        if (variables != null) {
            variables.forEach(context::setVariable);
        }

        return templateEngine.render(templateRequestDTO);
    }
}
