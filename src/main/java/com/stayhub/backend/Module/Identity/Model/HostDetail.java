package com.stayhub.backend.Module.Identity.Model;

import com.stayhub.backend.Common.Model.AbstractEntity;
import com.stayhub.backend.Common.Util.HostOnboardingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "host_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostDetail{
    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "about_host", columnDefinition = "TEXT")
    private String aboutHost;

    @Column(name = "business_phone", length = 15)
    private String businessPhone;

    @Column(name = "support_email", length = 100)
    private String supportEmail;

    @Column(name = "identity_card_number", length = 20)
    private String identityCardNumber;

    @Column(name = "identity_card_front_url", columnDefinition = "TEXT")
    private String identityCardFrontUrl;

    @Column(name = "identity_card_back_url", columnDefinition = "TEXT")
    private String identityCardBackUrl;

    @Column(name = "business_license_number", length = 50)
    private String businessLicenseNumber;

    @Column(name = "business_license_url", columnDefinition = "TEXT")
    private String businessLicenseUrl;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "onboarding_status", nullable = false)
    @Builder.Default
    private HostOnboardingStatus onboardingStatus = HostOnboardingStatus.DRAFT;

    @Column(name = "review_note", columnDefinition = "TEXT")
    private String reviewNote;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;
}
