package vn.coursebooking.consultant;

public record Consultant(
        Long id,
        String fullName,
        String expertise,
        String bio
) {
}