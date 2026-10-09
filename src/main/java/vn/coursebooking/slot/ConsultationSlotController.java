package vn.coursebooking.slot;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/slots")
public class ConsultationSlotController {

    private final ConsultationSlotService slotService;

    public ConsultationSlotController(
            ConsultationSlotService slotService
    ) {
        this.slotService = slotService;
    }

    @PostMapping
    public ResponseEntity<ConsultationSlot> createSlot(
            @Valid @RequestBody CreateSlotRequest request
    ) {
        ConsultationSlot slot = slotService.createSlot(request);

        return ResponseEntity.status(201).body(slot);
    }

    @GetMapping
    public List<ConsultationSlot> getSlots(
            @RequestParam Long consultantId
    ) {
        return slotService.getSlots(consultantId);
    }
}