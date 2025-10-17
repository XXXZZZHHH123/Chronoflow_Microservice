package nus.edu.u.domain.dataObject.push;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nus.edu.u.domain.dataObject.common.BaseNotificationEntity;
import nus.edu.u.domain.dataObject.common.NotificationDeliveryDO;
import nus.edu.u.enums.push.PushStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "push_message")
public class PushMessageDO extends BaseNotificationEntity {

    /** Shared primary key = FK to notification_delivery.id */
    @Id
    @Column(name = "delivery_id", length = 36, nullable = false)
    private String deliveryId;

    /** Back-reference to master delivery row */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(
            name = "delivery_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_push_delivery")
    )
    private NotificationDeliveryDO delivery;

    /** Device token used for FCM/APNs */
    @Column(length = 512)
    private String token;

    /** FCM message ID returned by Firebase */
    @Column(name = "fcm_id", length = 200)
    private String fcmId;

    /** PENDING | SENT | FAILED */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private PushStatus status = PushStatus.PENDING;

    /** Error message (nullable) */
    @Lob
    private String errorMessage;


    // --- convenient helpers ---
    public PushMessageDO markSent(String fcmId) {
        this.status = PushStatus.SENT;
        this.fcmId = fcmId;
        this.errorMessage = null;
        return this;
    }

    public PushMessageDO markFailed(String error) {
        this.status = PushStatus.FAILED;
        this.errorMessage = error;
        this.fcmId = null;
        return this;
    }
}