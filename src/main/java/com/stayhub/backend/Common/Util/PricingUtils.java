package com.stayhub.backend.Common.Util;

import com.stayhub.backend.Module.Property.Model.Room;
import com.stayhub.backend.Module.Property.Model.RoomAvailability;

import java.math.BigDecimal;
import java.time.DayOfWeek;

public class PricingUtils {
    public static BigDecimal calculateDailyPrice(Room room, RoomAvailability availability, BigDecimal surchargeMultiplier) {
        BigDecimal dailyPrice = room.getPricePerNight();
        if (availability.getPriceModifier() != null && availability.getPriceModifier().compareTo(BigDecimal.ZERO) > 0) {
            dailyPrice = availability.getPriceModifier();
        }

        DayOfWeek day = availability.getDate().getDayOfWeek();
        if (day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY) {
            dailyPrice = dailyPrice.multiply(surchargeMultiplier);
        }

        return dailyPrice;
    }
}
