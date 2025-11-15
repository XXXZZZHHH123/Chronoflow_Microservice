package nus.edu.u.services.notification;

import nus.edu.u.domain.dto.template.TemplateRequestDTO;
import nus.edu.u.domain.dto.template.TemplateResponseDTO;

public interface TemplateEngineImplementor {
    TemplateResponseDTO process(TemplateRequestDTO templateRequestDTO);
}
