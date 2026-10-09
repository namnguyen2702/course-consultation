package vn.coursebooking.slot;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record CreateSlotRequest(
        @NotNull(message = "Cần chọn tư vấn viên")
        @Positive(message = "ID tư vấn viên phải lớn hơn 0")
        Long consultantId,

        @NotNull(message = "Cần nhập giờ bắt đầu")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime startAt
) {
}