package com.stayhub.backend.Module.Property.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cancellation_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancellationPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "refund_percentage", nullable = false)
    private Integer refundPercentage;

    @Column(name = "days_before_checkin", nullable = false)
    private Integer daysBeforeCheckin;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
