package vn.coursebooking.consultant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateConsultantRequest(
        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
        String fullName,

        @NotBlank(message = "Lĩnh vực tư vấn không được để trống")
        @Size(max = 200, message = "Lĩnh vực tối đa 200 ký tự")
        String expertise,

        @Size(max = 2000, message = "Giới thiệu tối đa 2000 ký tự")
        String bio
) {
}