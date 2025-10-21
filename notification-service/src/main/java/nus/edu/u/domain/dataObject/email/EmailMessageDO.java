package nus.edu.u.domain.dataObject.email;

import jakarta.persistence.*;
import lombok.*;
import nus.edu.u.domain.dataObject.common.BaseNotificationEntity;
import nus.edu.u.domain.dataObject.common.NotificationDeliveryDO;
import nus.edu.u.enums.email.EmailProvider;
import nus.edu.u.enums.email.EmailStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "delivery")
@Entity
@Table(name = "email_message")
public class EmailMessageDO extends BaseNotificationEntity {

    /** Primary key doubles as FK to notification_delivery.id */
    @Id
    @Column(name = "delivery_id", length = 36, nullable = false)
    private String deliveryId;

    /** Back-reference to master delivery row (no @MapsId to avoid duplicate INSERTs) */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "delivery_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(name = "fk_email_delivery"))
    private NotificationDeliveryDO delivery;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EmailProvider provider = EmailProvider.AWS_SES;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private EmailStatus status = EmailStatus.PENDING;

    @Column(name = "provider_message_id", length = 200)
    private String providerMessageId;

    @Lob
    @Column(name = "error_message")
    private String errorMessage;

    @PrePersist
    void syncPkFromParent() {
        if (this.deliveryId == null && this.delivery != null) {
            this.deliveryId = this.delivery.getId();
        }
    }

    // Convenience helpers
    public EmailMessageDO markSent(String providerMessageId) {
        this.status = EmailStatus.SENT;
        this.providerMessageId = providerMessageId;
        this.errorMessage = null;
        return this;
    }

    public EmailMessageDO markFailed(String error) {
        this.status = EmailStatus.FAILED;
        this.errorMessage = error;
        this.providerMessageId = null;
        return this;
    }
}
