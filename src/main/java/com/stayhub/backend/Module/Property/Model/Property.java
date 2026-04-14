package com.stayhub.backend.Module.Property.Model;

import com.stayhub.backend.Common.Util.PropertyStatus;
import com.stayhub.backend.Common.Util.StringUtil;
import com.stayhub.backend.Module.Identity.Model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quan hệ với Host, Category, RentalType
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_type_id", nullable = false)
    private RentalType rentalType;

    // Thông tin cơ bản
    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "address_detail")
    private String addressDetail;
    private String ward;
    private String district;
    private String province;

    @Column(name = "cleaning_fee")
    @Builder.Default
    private BigDecimal cleaningFee = BigDecimal.ZERO;

    @Column(name = "weekend_surcharge_percentage")
    @Builder.Default
    private Integer weekendSurchargePercentage = 0;

    @Column(name = "is_pay_at_checkin_allowed")
    @Builder.Default
    private Boolean isPayAtCheckinAllowed = true;

    @Column(name = "deposit_percentage")
    @Builder.Default
    private Integer depositPercentage = 0;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    @Builder.Default
    private PropertyStatus status = PropertyStatus.DRAFT;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PropertyImage> images = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "property_amenities",
            joinColumns = @JoinColumn(name = "property_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    @Builder.Default
    private Set<Amenity> amenities = new HashSet<>();

    @Builder.Default
    @Column(name = "room_count")
    private Integer roomCount = 1;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    // ==========================================
    // THỜI GIAN NHẬN/TRẢ PHÒNG & TIỀN TỆ
    // ==========================================
    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "checkin_after", length = 10)
    @Builder.Default
    private String checkinAfter = "14:00";

    @Column(name = "checkin_before", length = 10)
    @Builder.Default
    private String checkinBefore = "23:30";

    @Column(name = "checkout_after", length = 10)
    @Builder.Default
    private String checkoutAfter = "01:00";

    @Column(name = "checkout_before", length = 10)
    @Builder.Default
    private String checkoutBefore = "12:00";
    // ==========================================
    // NỘI QUY CHỖ Ở & CÀI ĐẶT ĐẶT PHÒNG
    // ==========================================
    @Column(name = "is_instant_book")
    @Builder.Default
    private Boolean isInstantBook = true;

    @Column(name = "is_smoking_allowed")
    @Builder.Default
    private Boolean isSmokingAllowed = true;

    @Column(name = "is_pets_allowed")
    @Builder.Default
    private Boolean isPetsAllowed = true;

    @Column(name = "is_party_allowed")
    @Builder.Default
    private Boolean isPartyAllowed = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancellation_policy_id")
    private CancellationPolicy cancellationPolicy;

    // ==========================================
    // THỐNG KÊ ĐÁNH GIÁ (Dùng để hiển thị ngoài Card)
    // ==========================================
    @Column(name = "rating_avg")
    @Builder.Default
    private Double ratingAvg = 0.0;

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;

    @Column(name = "search_text", columnDefinition = "TEXT")
    private String searchText;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Room> rooms = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void generateSearchText() {
        String rawData = String.format("%s %s %s %s",
                this.name != null ? this.name : "",
                this.province != null ? this.province : "",
                this.district != null ? this.district : "",
                this.ward != null ? this.ward : ""
        );
        this.searchText = StringUtil.normalizeForSearch(rawData);
    }
}
