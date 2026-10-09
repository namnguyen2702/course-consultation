package vn.coursebooking.application;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalTime;
import java.util.Set;

public record ConsultantApplicationRequest(
        @NotBlank @Pattern(regexp = "[+0-9 ()-]{9,20}", message = "Số điện thoại không hợp lệ") String phone,
        @NotBlank @Size(max = 200) String expertise,
        @NotNull @Min(0) @Max(60) Integer experienceYears,
        @NotBlank @Size(max = 2000) String bio,
        @Size(max = 500) String portfolioUrl,
        @NotEmpty(message = "Chọn ít nhất một ngày làm việc") Set<@Min(1) @Max(7) Integer> workDays,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime availableFrom,
        @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime availableUntil,
        @NotEmpty(message = "Chọn ít nhất một khóa học có thể tư vấn") Set<@Positive Long> courseIds
) {
    public static ConsultantApplicationRequest empty() {
        return new ConsultantApplicationRequest("", "", 0, "", "", Set.of(),
                LocalTime.of(9, 0), LocalTime.of(17, 0), Set.of());
    }
}
