package vn.coursebooking.slot;

import java.time.LocalDateTime;

public record ConsultationSlot(
        Long id,
        Long consultantId,
        LocalDateTime startAt,
        LocalDateTime endAt
) {
}