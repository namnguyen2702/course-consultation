package vn.coursebooking.user;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String role
) {
}