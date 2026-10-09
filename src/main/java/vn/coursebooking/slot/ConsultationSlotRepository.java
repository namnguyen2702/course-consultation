package vn.coursebooking.slot;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConsultationSlotRepository
        extends JpaRepository<ConsultationSlotEntity, Long> {

    boolean existsByConsultantIdAndStartAt(
            Long consultantId,
            LocalDateTime startAt
    );

    @Query("""
        SELECT slot
        FROM ConsultationSlotEntity slot
        WHERE slot.consultantId = :consultantId
          AND slot.active = true
          AND slot.startAt > :now
          AND NOT EXISTS (
              SELECT booking.id
              FROM BookingEntity booking
              WHERE booking.slotId = slot.id
                AND booking.status <> 'CANCELLED'
          )
        ORDER BY slot.startAt ASC
        """)
    List<ConsultationSlotEntity>
    findByConsultantIdAndActiveTrueAndStartAtAfterOrderByStartAtAsc(
            @Param("consultantId") Long consultantId,
            @Param("now") LocalDateTime now
    );
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT slot FROM ConsultationSlotEntity slot WHERE slot.id = :id")
    Optional<ConsultationSlotEntity> findForUpdate(
            @Param("id") Long id
    );
}