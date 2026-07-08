package com.stayhub.backend.Module.Property.Service;

import com.stayhub.backend.Module.Property.DTO.Request.CalendarUpdateRequest;
import com.stayhub.backend.Module.Property.DTO.Response.CalendarDayResponse;

import java.util.List;

public interface RoomService {
    void refreshDailyAvailability();
    List<CalendarDayResponse> getRoomCalendar(Long hostId, Long roomId, int year, int month);
    void updateRoomCalendar(Long hostId, Long roomId, CalendarUpdateRequest request);
}
