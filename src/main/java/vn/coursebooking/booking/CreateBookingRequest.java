package vn.coursebooking.booking;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateBookingRequest(
        @NotNull(message = "Cần chọn khóa học")
        @Positive(message = "ID khóa học phải lớn hơn 0")
        Long courseId,

        @NotNull(message = "Cần chọn khung giờ")
        @Positive(message = "ID khung giờ phải lớn hơn 0")
        Long slotId
) {
}