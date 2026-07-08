package com.stayhub.backend.Module.Property.Model;

import com.stayhub.backend.Common.Model.AbstractEntity;
import com.stayhub.backend.Common.Util.SubscriptionTier;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlan extends AbstractEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, unique = true)
    private SubscriptionTier tier;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(name = "duration_months")
    private Integer durationMonths;

    @Column(name = "max_listings")
    private Integer maxListings;

    @Column(name = "commission_rate", precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @Column(name = "credit_limit", nullable = false)
    private BigDecimal creditLimit;

    @Column(name = "is_active", insertable = false)
    private Boolean isActive;
}
