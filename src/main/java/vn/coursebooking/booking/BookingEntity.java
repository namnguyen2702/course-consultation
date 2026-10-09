package vn.coursebooking.booking;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "bookings")
public class BookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @Column(name = "slot_id", nullable = false)
    private Long slotId;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected BookingEntity() {
    }

    public BookingEntity(Long userId, Long courseId, Long slotId) {
        this.userId = userId;
        this.courseId = courseId;
        this.slotId = slotId;
        this.status = "CONFIRMED";
        this.createdAt = LocalDateTime.now(
                ZoneId.of("Asia/Ho_Chi_Minh")
        );
    }

    public void cancel() {
        this.status = "CANCELLED";
    }

    public void finish(String status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public Long getSlotId() {
        return slotId;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}