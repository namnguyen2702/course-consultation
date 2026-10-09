package vn.coursebooking.consultant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConsultantService {

    private final ConsultantRepository consultantRepository;

    public ConsultantService(ConsultantRepository consultantRepository) {
        this.consultantRepository = consultantRepository;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<Consultant> getConsultantsForCourse(Long courseId) {
        return consultantRepository.findEligibleForCourse(courseId).stream()
                .map(this::toConsultant).toList();
    }

    public List<Consultant> getAllConsultants() {
        List<ConsultantEntity> entities =
                consultantRepository.findByActiveTrueOrderByIdAsc();

        List<Consultant> consultants = new ArrayList<>();

        for (ConsultantEntity entity : entities) {
            consultants.add(toConsultant(entity));
        }

        return consultants;
    }

    public Consultant createConsultant(CreateConsultantRequest request) {
        ConsultantEntity entity = new ConsultantEntity(
                request.fullName(),
                request.expertise(),
                request.bio()
        );

        ConsultantEntity savedEntity = consultantRepository.save(entity);

        return toConsultant(savedEntity);
    }

    private Consultant toConsultant(ConsultantEntity entity) {
        return new Consultant(
                entity.getId(),
                entity.getFullName(),
                entity.getExpertise(),
                entity.getBio()
        );
    }

    public Consultant getConsultantById(Long id) {
        ConsultantEntity entity = consultantRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tư vấn viên"
                ));

        return toConsultant(entity);
    }

    public Consultant updateConsultant(
            Long id,
            CreateConsultantRequest request
    ) {
        ConsultantEntity entity = consultantRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy tư vấn viên"
                ));

        entity.updateInfo(
                request.fullName(),
                request.expertise(),
                request.bio()
        );

        ConsultantEntity savedEntity =
                consultantRepository.save(entity);

        return toConsultant(savedEntity);
    }
}