package vn.coursebooking.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository
        extends JpaRepository<BookingEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BookingEntity> findByIdAndUserId(Long id, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT b FROM BookingEntity b WHERE b.id = :id")
    Optional<BookingEntity> findForUpdate(@org.springframework.data.repository.query.Param("id") Long id);

    List<BookingEntity> findAllByOrderByCreatedAtDescIdDesc();

    @Query("""
        SELECT b FROM BookingEntity b, ConsultationSlotEntity s
        WHERE b.slotId = s.id AND s.consultantId = :consultantId
        ORDER BY s.startAt DESC, b.id DESC
        """)
    List<BookingEntity> findForConsultant(@Param("consultantId") Long consultantId);

    boolean existsBySlotIdAndStatusNot(Long slotId, String status);

    List<BookingEntity> findByUserIdOrderByCreatedAtDescIdDesc(Long userId);

    @Query("""
    SELECT COUNT(booking)
    FROM BookingEntity booking, ConsultationSlotEntity slot
    WHERE booking.slotId = slot.id
      AND booking.userId = :userId
      AND booking.status <> 'CANCELLED'
      AND slot.startAt = :startAt
    """)
    long countBookingsAtTime(
            @Param("userId") Long userId,
            @Param("startAt") LocalDateTime startAt
    );
}