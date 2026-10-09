package vn.coursebooking.application;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "consultant_applications")
public class ConsultantApplicationEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false) private Long userId;
    @Column(nullable = false, length = 20) private String phone;
    @Column(nullable = false, length = 200) private String expertise;
    @Column(name = "experience_years", nullable = false) private Integer experienceYears;
    @Column(nullable = false, columnDefinition = "text") private String bio;
    @Column(name = "portfolio_url", length = 500) private String portfolioUrl;
    @Column(name = "available_from", nullable = false) private LocalTime availableFrom;
    @Column(name = "available_until", nullable = false) private LocalTime availableUntil;
    @Column(nullable = false, length = 20) private String status = "PENDING";
    @Column(name = "review_note", length = 500) private String reviewNote;
    @Column(name = "reviewed_by") private Long reviewedBy;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "reviewed_at") private LocalDateTime reviewedAt;
    @ElementCollection
    @CollectionTable(name = "application_work_days", joinColumns = @JoinColumn(name = "application_id"))
    @Column(name = "day_of_week") private Set<Integer> workDays = new HashSet<>();
    @ElementCollection
    @CollectionTable(name = "application_courses", joinColumns = @JoinColumn(name = "application_id"))
    @Column(name = "course_id") private Set<Long> courseIds = new HashSet<>();

    protected ConsultantApplicationEntity() {}
    public ConsultantApplicationEntity(Long userId, ConsultantApplicationRequest request) {
        this.userId = userId;
        this.phone = request.phone().strip();
        this.expertise = request.expertise().strip();
        this.experienceYears = request.experienceYears();
        this.bio = request.bio().strip();
        this.portfolioUrl = request.portfolioUrl() == null ? "" : request.portfolioUrl().strip();
        this.availableFrom = request.availableFrom();
        this.availableUntil = request.availableUntil();
        this.workDays = new HashSet<>(request.workDays());
        this.courseIds = new HashSet<>(request.courseIds());
        this.createdAt = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
    }
    public void review(String status, String note, Long reviewer) {
        this.status = status;
        this.reviewNote = note;
        this.reviewedBy = reviewer;
        this.reviewedAt = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
    }
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getPhone() { return phone; }
    public String getExpertise() { return expertise; }
    public Integer getExperienceYears() { return experienceYears; }
    public String getBio() { return bio; }
    public String getPortfolioUrl() { return portfolioUrl; }
    public LocalTime getAvailableFrom() { return availableFrom; }
    public LocalTime getAvailableUntil() { return availableUntil; }
    public String getStatus() { return status; }
    public String getReviewNote() { return reviewNote; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public Set<Integer> getWorkDays() { return workDays; }
    public Set<Long> getCourseIds() { return courseIds; }
}
