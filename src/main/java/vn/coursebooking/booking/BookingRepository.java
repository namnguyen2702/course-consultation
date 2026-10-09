package vn.coursebooking.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

public interface BookingRepository
        extends JpaRepository<BookingEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BookingEntity> findByIdAndUserId(Long id, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT b FROM BookingEntity b WHERE b.id = :id")
    Optional<BookingEntity> findForUpdate(@org.springframework.data.repository.query.Param("id") Long id);

    List<BookingEntity> findAllByOrderByCreatedAtDescIdDesc();

    boolean existsBySlotIdAndStatusNot(Long slotId, String status);

    List<BookingEntity> findByUserIdOrderByCreatedAtDescIdDesc(Long userId);
}