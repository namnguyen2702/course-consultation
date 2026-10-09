package vn.coursebooking.consultant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultantRepository
        extends JpaRepository<ConsultantEntity, Long> {

    List<ConsultantEntity> findByActiveTrueOrderByIdAsc();

    boolean existsByIdAndActiveTrue(Long id);

    java.util.Optional<ConsultantEntity> findByUserId(Long userId);

    @org.springframework.data.jpa.repository.Query("""
            SELECT c FROM ConsultantEntity c
            WHERE c.active = true AND (c.userId IS NULL OR :courseId MEMBER OF c.courseIds)
            ORDER BY c.id ASC
            """)
    List<ConsultantEntity> findEligibleForCourse(@org.springframework.data.repository.query.Param("courseId") Long courseId);
}