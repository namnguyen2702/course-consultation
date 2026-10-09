package vn.coursebooking.consultant;

import jakarta.persistence.*;

@Entity
@Table(name = "consultants")
public class ConsultantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 200)
    private String expertise;

    @Column(columnDefinition = "text")
    private String bio;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "user_id", unique = true)
    private Long userId;

    @Column(name = "available_from")
    private java.time.LocalTime availableFrom;

    @Column(name = "available_until")
    private java.time.LocalTime availableUntil;

    @ElementCollection
    @CollectionTable(name = "consultant_work_days", joinColumns = @JoinColumn(name = "consultant_id"))
    @Column(name = "day_of_week")
    private java.util.Set<Integer> workDays = new java.util.HashSet<>();

    @ElementCollection
    @CollectionTable(name = "consultant_courses", joinColumns = @JoinColumn(name = "consultant_id"))
    @Column(name = "course_id")
    private java.util.Set<Long> courseIds = new java.util.HashSet<>();

    public void linkApprovedAccount(Long userId, java.time.LocalTime from,
                                    java.time.LocalTime until, java.util.Set<Integer> days,
                                    java.util.Set<Long> courses) {
        this.userId = userId;
        this.availableFrom = from;
        this.availableUntil = until;
        this.workDays = new java.util.HashSet<>(days);
        this.courseIds = new java.util.HashSet<>(courses);
    }

    public Long getUserId() {
        return userId;
    }

    public boolean isActive() {
        return active;
    }

    public java.time.LocalTime getAvailableFrom() {
        return availableFrom;
    }

    public java.time.LocalTime getAvailableUntil() {
        return availableUntil;
    }

    public java.util.Set<Integer> getWorkDays() {
        return workDays;
    }

    public java.util.Set<Long> getCourseIds() {
        return courseIds;
    }

    public boolean canConsultCourse(Long courseId) {
        return userId == null || courseIds.contains(courseId);
    }

    public boolean acceptsStartAt(java.time.LocalDateTime startAt) {
        if (userId == null) return true; // Keep existing manually managed demo profiles.
        java.time.LocalDateTime endAt = startAt.plusMinutes(30);
        return workDays.contains(startAt.getDayOfWeek().getValue())
                && startAt.toLocalDate().equals(endAt.toLocalDate())
                && !startAt.toLocalTime().isBefore(availableFrom)
                && !endAt.toLocalTime().isAfter(availableUntil);
    }

    protected ConsultantEntity() {
    }

    public ConsultantEntity(String fullName, String expertise, String bio) {
        this.fullName = fullName;
        this.expertise = expertise;
        this.bio = bio;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getExpertise() {
        return expertise;
    }

    public String getBio() {
        return bio;
    }

    public void updateInfo(
            String fullName,
            String expertise,
            String bio
    ) {
        this.fullName = fullName;
        this.expertise = expertise;
        this.bio = bio;
    }
}