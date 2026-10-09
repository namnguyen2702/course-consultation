package vn.coursebooking.booking;

import java.time.LocalDateTime;

public record MyBooking(
        Long id,
        String courseName,
        String consultantName,
        LocalDateTime startAt,
        LocalDateTime endAt,
        String status
) {
}