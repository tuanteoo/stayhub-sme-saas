package com.stayhub.backend.Module.Identity.DTO.Response;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record PropertyDetailResponse(
        Long id,
        String name,
        String slug,
        String description,

        // Vị trí
        String addressDetail,
        String ward,
        String district,
        String province,
        Double latitude,
        Double longitude,

        // Cấu trúc & Tiện ích
        Integer maxGuests,
        Integer numBedrooms,
        Integer numBeds,
        Integer numBathrooms,
        List<String> amenities,
        List<String> imageUrls,

        // Tài chính
        BigDecimal pricePerNight,
        BigDecimal cleaningFee,
        Integer weekendSurchargePercentage,
        Integer depositPercentage,
        Boolean isPayAtCheckinAllowed,

        // Nội quy & Thời gian
        String checkinAfter,
        String checkoutBefore,
        Boolean isInstantBook,
        Boolean isSmokingAllowed,
        Boolean isPetsAllowed,
        Boolean isPartyAllowed,

        // Thống kê đánh giá
        Double ratingAvg,
        Integer reviewCount,

        String categoryName,
        String rentalTypeName,

        HostInfoResponse host,

        CancellationPolicyResponse cancellationPolicy
) {
}
