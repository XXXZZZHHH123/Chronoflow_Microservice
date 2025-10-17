package nus.edu.u.domain.dto.common;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import nus.edu.u.enums.common.NotificationChannel;
import nus.edu.u.enums.common.NotificationEventType;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Builder
public record NotificationRequestDTO(

        /** Channel to send through (EMAIL, PUSH, WS) */
        @NotNull NotificationChannel channel,

        /** Recipient address or user target (email, device token, etc.) */
        String to,

        /** Optional higher-level user identifier (for PUSH / WS fan-out) */
        String userId,

        /** Optional derived identifier (e.g. "email:xxx", "ws:user123") */
        String recipientKey,

        /** Template identifier (e.g. "organizer-welcome", "member-invite") */
        String templateId,

        /** Dynamic variables for the template */
        Map<String, Object> variables,

        /** Locale for i18n templates */
        Locale locale,

        /** Optional attachments (images, PDFs, inline resources) */
        List<AttachmentDTO> attachments,

        /** Required for idempotency — unique per logical event */
        @NotBlank String eventId,

        /** Notification type (enum) — ORGANIZER_WELCOME, MEMBER_INVITE, ATTENDEE_INVITE */
        @NotNull NotificationEventType type
) {}