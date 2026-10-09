package vn.coursebooking.slot;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultation_slots")
public class ConsultationSlotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consultant_id", nullable = false)
    private Long consultantId;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private boolean active = true;

    protected ConsultationSlotEntity() {
    }

    public ConsultationSlotEntity(
            Long consultantId,
            LocalDateTime startAt
    ) {
        this.consultantId = consultantId;
        this.startAt = startAt;
    }

    public Long getId() {
        return id;
    }

    public Long getConsultantId() {
        return consultantId;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }
    public boolean isActive() {
        return active;
    }
    public void deactivate(){
        this.active = false;
    }
}