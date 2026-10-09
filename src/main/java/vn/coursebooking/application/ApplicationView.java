package vn.coursebooking.application;
import java.time.*;
import java.util.List;

public record ApplicationView(Long id, String fullName, String email, String phone,
        String expertise, Integer experienceYears, String bio, String portfolioUrl,
        String workDays, LocalTime availableFrom, LocalTime availableUntil,
        List<String> courseNames, String status, String reviewNote, LocalDateTime createdAt) {}
