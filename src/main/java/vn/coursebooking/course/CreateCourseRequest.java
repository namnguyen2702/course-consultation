package vn.coursebooking.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateCourseRequest(
        @NotBlank(message = "Tên khóa học không được để trống")
                @Size(max =  150, message = "Toi da 150 ki tu")
        String name,
        String description,
        String prerequisites,

        @Positive(message = "So tuan hoc phai lon hon 0")
        int durationWeeks
) {
}
