package vn.coursebooking.booking;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateBookingStatusRequest(
        @NotBlank(message = "Cần chọn trạng thái")
        @Pattern(regexp = "COMPLETED|NO_SHOW", message = "Chỉ chấp nhận COMPLETED hoặc NO_SHOW")
        String status
) {
}