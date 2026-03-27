package com.stayhub.backend.Module.Property.Mapper;

import com.stayhub.backend.Module.Property.DTO.Response.AmenityResponse;
import com.stayhub.backend.Module.Property.DTO.Response.CancellationPolicyResponse;
import com.stayhub.backend.Module.Property.DTO.Response.RoomResponse;
import com.stayhub.backend.Module.Property.Model.Room;
import com.stayhub.backend.Module.Property.Model.RoomImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RoomMapper {
    private final AmenityMapper amenityMapper;
    private final CancellationPolicyMapper cancellationPolicyMapper;

    public RoomResponse toResponse(Room room) {
        if (room == null) return null;

        List<AmenityResponse> roomAmenities = room.getAmenities().stream()
                .map(amenityMapper::toResponse)
                .toList();

        String thumbnailUrl = room.getImages().stream()
                .filter(RoomImage::getIsThumbnail)
                .map(RoomImage::getUrl)
                .findFirst()
                .orElse(null);

        CancellationPolicyResponse policyResponse = cancellationPolicyMapper.toResponse(room.getCancellationPolicy());

        // 👉 Trả về kết quả cuối cùng
        return new RoomResponse(
                room.getId(),
                room.getName(),
                room.getDescription(),
                room.getPricePerNight(),
                room.getMaxGuests(),
                room.getNumBeds(),
                room.getNumBathrooms(),
                roomAmenities,
                thumbnailUrl,
                policyResponse
        );
    }
}
