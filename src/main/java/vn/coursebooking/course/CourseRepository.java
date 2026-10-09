package vn.coursebooking.course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository
        extends JpaRepository<CourseEntity, Long> {

    List<CourseEntity> findByActiveTrueOrderByIdAsc();

    Optional<CourseEntity> findByIdAndActiveTrue(Long id);

    boolean existsByIdAndActiveTrue(Long id);
}