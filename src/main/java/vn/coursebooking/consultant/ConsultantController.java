package vn.coursebooking.consultant;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultants")
public class ConsultantController {

    private final ConsultantService consultantService;

    public ConsultantController(ConsultantService consultantService) {
        this.consultantService = consultantService;
    }

    @GetMapping
    public List<Consultant> getAllConsultants() {
        return consultantService.getAllConsultants();
    }

    @PostMapping
    public ResponseEntity<Consultant> createConsultant(
            @Valid @RequestBody CreateConsultantRequest request
    ) {
        Consultant consultant = consultantService.createConsultant(request);

        return ResponseEntity.status(201).body(consultant);
    }
}