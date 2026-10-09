package vn.coursebooking.consultant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultantRepository
        extends JpaRepository<ConsultantEntity, Long> {

    List<ConsultantEntity> findByActiveTrueOrderByIdAsc();
    boolean existsByIdAndActiveTrue(Long id);
}