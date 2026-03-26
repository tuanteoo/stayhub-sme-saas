package com.stayhub.backend.Module.Property.DTO.Response;

import com.stayhub.backend.Module.Identity.DTO.Response.CancellationPolicyResponse;
import com.stayhub.backend.Module.Identity.DTO.Response.HostInfoResponse;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record PropertyDetailResponse(
        Long id,
        String name,
        String slug,
        String description,

        String addressDetail,
        String ward,
        String district,
        String province,
        Double latitude,
        Double longitude,

        Integer maxGuests,
        Integer numBedrooms,
        Integer numBeds,
        Integer numBathrooms,

        BigDecimal cleaningFee,
        Integer weekendSurchargePercentage,
        Integer depositPercentage,
        Boolean isPayAtCheckinAllowed,

        String checkinAfter,
        String checkoutBefore,
        Boolean isInstantBook,
        Boolean isSmokingAllowed,
        Boolean isPetsAllowed,
        Boolean isPartyAllowed,

        Double ratingAvg,
        Integer reviewCount,

        String categoryName,
        String rentalTypeName,

        HostInfoResponse host,
        CancellationPolicyResponse cancellationPolicy,

        List<AmenityResponse> amenities,
        List<String> imageUrls,

        List<RoomResponse> rooms
) {
}
