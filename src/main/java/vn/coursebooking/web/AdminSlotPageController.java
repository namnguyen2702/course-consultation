package vn.coursebooking.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.coursebooking.consultant.ConsultantService;
import vn.coursebooking.slot.ConsultationSlotService;
import vn.coursebooking.slot.CreateSlotRequest;

import java.util.List;

@Controller
public class AdminSlotPageController {

    private final ConsultantService consultantService;
    private final ConsultationSlotService slotService;

    public AdminSlotPageController(
            ConsultantService consultantService,
            ConsultationSlotService slotService
    ) {
        this.consultantService = consultantService;
        this.slotService = slotService;
    }

    @GetMapping("/admin/slots")
    public String slots(
            @RequestParam(required = false) Long consultantId,
            Model model
    ) {
        model.addAttribute(
                "consultants",
                consultantService.getAllConsultants()
        );

        model.addAttribute(
                "slotForm",
                new CreateSlotRequest(consultantId, null)
        );

        model.addAttribute("selectedConsultantId", consultantId);

        model.addAttribute(
                "slots",
                consultantId == null
                        ? List.of()
                        : slotService.getSlots(consultantId)
        );

        return "admin-slots";
    }

    @PostMapping("/admin/slots")
    public String createSlot(
            @Valid @ModelAttribute("slotForm") CreateSlotRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (!bindingResult.hasErrors()) {
            try {
                slotService.createSlot(request);

                redirectAttributes.addFlashAttribute(
                        "successMessage",
                        "Đã tạo khung giờ."
                );

                redirectAttributes.addAttribute(
                        "consultantId",
                        request.consultantId()
                );

                return "redirect:/admin/slots";

            } catch (ResponseStatusException exception) {
                model.addAttribute(
                        "errorMessage",
                        exception.getReason()
                );
            }
        }

        model.addAttribute(
                "consultants",
                consultantService.getAllConsultants()
        );

        model.addAttribute("slots", List.of());
        model.addAttribute("selectedConsultantId", null);

        return "admin-slots";
    }
}