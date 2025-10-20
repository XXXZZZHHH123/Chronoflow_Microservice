package nus.edu.u.domain.dto.email;

import nus.edu.u.enums.email.EmailProvider;

public record EmailSendResultDTO(EmailProvider provider, String providerMessageId) {}
