package com.stayhub.backend.Module.Property.Model;

import com.stayhub.backend.Common.Util.AmenityType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "amenities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Amenity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "icon_name", nullable = false)
    private String iconName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AmenityType type;
}
