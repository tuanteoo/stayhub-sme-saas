package com.stayhub.backend.Module.Property.Model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rental_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false, unique = true)
    private String name;

    @Column(length = 100, nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_name", nullable = false)
    private String iconName;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
