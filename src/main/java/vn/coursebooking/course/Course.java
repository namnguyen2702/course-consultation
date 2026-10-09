package vn.coursebooking.course;

// Dữ liệu trả về cho người dùng; ở bước này chưa phải bảng trong database.
public record Course(
        Long id,
        String name,
        String description,
        String prerequisites,
        int durationWeeks
) {
}
