package nus.edu.u.services.email;

import nus.edu.u.domain.dto.email.EmailRequestDTO;

public interface EmailService {
    String send(EmailRequestDTO dto);
}
