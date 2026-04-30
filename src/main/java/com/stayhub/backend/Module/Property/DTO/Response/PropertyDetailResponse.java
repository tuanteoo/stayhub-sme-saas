package com.stayhub.backend.Module.Property.DTO.Response;

import com.stayhub.backend.Module.Identity.DTO.Response.HostInfoResponse;
import com.stayhub.backend.Module.Review.DTO.Response.ReviewResponse;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record PropertyDetailResponse(
        Long id,
        String name,
        String slug,

        List<String> imageUrls,
        List<AmenityResponse> amenities,
        HostInfoResponse host,

        String description,
        String addressDetail,
        String ward,
        String district,
        String province,
        Double latitude,
        Double longitude,

        Integer roomCount,
        Integer maxGuests,
        Integer numBedrooms,
        Integer numBeds,
        Integer numBathrooms,

        BigDecimal cleaningFee,
        Integer weekendSurchargePercentage,
        Boolean isPayAtCheckinAllowed,
        Integer depositPercentage,
        CancellationPolicyResponse cancellationPolicyResponse,

        String checkInAfter,
        String checkInBefore,
        String checkOutAfter,
        String checkOutBefore,
        Boolean isInstantBook,
        Boolean isSmokingAllowed,
        Boolean isPetsAllowed,
        Boolean isPartyAllowed,

        Double ratingAvg,
        Integer reviewCount,

        String categoryName,
        String rentalTypeName,
        String rentalTypeSlug,

        List<RoomResponse> rooms,
        List<ReviewResponse> reviews
) {
}
