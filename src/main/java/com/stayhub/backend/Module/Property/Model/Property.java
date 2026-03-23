package com.stayhub.backend.Module.Property.Model;

import com.stayhub.backend.Common.Util.PropertyStatus;
import com.stayhub.backend.Module.Identity.Model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
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

    // Địa chỉ
    @Column(name = "address_detail")
    private String addressDetail;
    private String ward;
    private String district;
    private String province;

    // Cấu trúc
    @Column(name = "max_guests", nullable = false)
    private Integer maxGuests;

    @Column(name = "num_bedrooms")
    private Integer numBedrooms;

    @Column(name = "num_beds")
    private Integer numBeds;

    @Column(name = "num_bathrooms")
    private Integer numBathrooms;

    // Tài chính (Dùng BigDecimal cho tiền tệ là chuẩn nhất)
    @Column(name = "price_per_night", nullable = false)
    private BigDecimal pricePerNight;

    @Column(name = "cleaning_fee")
    @Builder.Default
    private BigDecimal cleaningFee = BigDecimal.ZERO;

    @Column(name = "weekend_surcharge_percentage")
    @Builder.Default
    private Integer weekendSurchargePercentage = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancellation_policy_id")
    private CancellationPolicy cancellationPolicy;

    @Column(name = "is_pay_at_checkin_allowed")
    @Builder.Default
    private Boolean isPayAtCheckinAllowed = false;

    @Column(name = "deposit_percentage")
    @Builder.Default
    private Integer depositPercentage = 100;

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
}
