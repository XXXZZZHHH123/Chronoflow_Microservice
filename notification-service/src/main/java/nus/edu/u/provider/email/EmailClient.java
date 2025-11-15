package nus.edu.u.provider.email;

import nus.edu.u.domain.dto.email.EmailRequestDTO;

public interface EmailClient {
    void sendEmail(EmailRequestDTO emailRequestDTO);
}
