package vn.coursebooking.slot;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record CreateSlotRequest(
        @NotNull(message = "Cần chọn tư vấn viên")
        @Positive(message = "ID tư vấn viên phải lớn hơn 0")
        Long consultantId,

        @NotNull(message = "Cần nhập giờ bắt đầu")
        LocalDateTime startAt
) {
}