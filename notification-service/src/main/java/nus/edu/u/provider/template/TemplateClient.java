package nus.edu.u.provider.template;


import nus.edu.u.domain.dto.template.TemplateRequestDTO;
import nus.edu.u.domain.dto.template.TemplateResponseDTO;

public interface TemplateClient {

    TemplateResponseDTO render(TemplateRequestDTO templateRequestDTO);
}
