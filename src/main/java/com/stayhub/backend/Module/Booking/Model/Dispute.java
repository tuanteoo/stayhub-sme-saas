package com.stayhub.backend.Module.Booking.Model;
import com.stayhub.backend.Common.Model.AbstractEntity;
import com.stayhub.backend.Common.Util.DisputeStatus;
import com.stayhub.backend.Module.Identity.Model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "disputes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dispute extends AbstractEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_dispute_bkg"))
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dispute_creator"))
    private User creator;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "evidence_image_urls", columnDefinition = "TEXT")
    private String evidenceImageUrls;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", columnDefinition = "dispute_status_enum", nullable = false)
    @Builder.Default
    private DisputeStatus status = DisputeStatus.OPEN;

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
