package vn.coursebooking.application;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface ConsultantApplicationRepository extends JpaRepository<ConsultantApplicationEntity, Long> {
    boolean existsByUserIdAndStatus(Long userId, String status);
    Optional<ConsultantApplicationEntity> findFirstByUserIdOrderByIdDesc(Long userId);
    List<ConsultantApplicationEntity> findAllByOrderByCreatedAtDescIdDesc();
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ConsultantApplicationEntity a WHERE a.id = :id")
    Optional<ConsultantApplicationEntity> findForUpdate(@Param("id") Long id);
}
