package vn.coursebooking.booking;

import java.time.LocalDateTime;

public record Booking(
        Long id,
        Long courseId,
        Long slotId,
        String status,
        LocalDateTime createdAt
) {
}