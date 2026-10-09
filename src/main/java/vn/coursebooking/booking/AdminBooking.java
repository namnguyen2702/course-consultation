package vn.coursebooking.booking;

import java.time.LocalDateTime;

public record AdminBooking(
        Long id, String customerName, String customerEmail,
        Long courseId, Long consultantId, Long slotId,
        LocalDateTime startAt, LocalDateTime endAt,
        String status, LocalDateTime createdAt
) {
}