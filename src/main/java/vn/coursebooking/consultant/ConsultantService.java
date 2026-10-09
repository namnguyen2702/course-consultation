package vn.coursebooking.consultant;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConsultantService {

    private final ConsultantRepository consultantRepository;

    public ConsultantService(ConsultantRepository consultantRepository) {
        this.consultantRepository = consultantRepository;
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
}