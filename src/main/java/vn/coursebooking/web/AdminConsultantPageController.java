package vn.coursebooking.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.coursebooking.consultant.ConsultantService;
import vn.coursebooking.consultant.CreateConsultantRequest;

@Controller
public class AdminConsultantPageController {

    private final ConsultantService consultantService;

    public AdminConsultantPageController(
            ConsultantService consultantService
    ) {
        this.consultantService = consultantService;
    }

    @GetMapping("/admin/consultants")
    public String consultants(Model model) {
        model.addAttribute(
                "consultants",
                consultantService.getAllConsultants()
        );

        model.addAttribute(
                "consultantForm",
                new CreateConsultantRequest("", "", "")
        );

        return "admin-consultants";
    }

    @PostMapping("/admin/consultants")
    public String createConsultant(
            @Valid @ModelAttribute("consultantForm")
            CreateConsultantRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "consultants",
                    consultantService.getAllConsultants()
            );

            return "admin-consultants";
        }

        consultantService.createConsultant(request);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Đã thêm tư vấn viên."
        );

        return "redirect:/admin/consultants";
    }
    @GetMapping("/admin/consultants/{id}/edit")
    public String editPage(
            @PathVariable Long id,
            Model model
    ) {
        var consultant = consultantService.getConsultantById(id);

        model.addAttribute("consultantId", id);

        model.addAttribute(
                "consultantForm",
                new CreateConsultantRequest(
                        consultant.fullName(),
                        consultant.expertise(),
                        consultant.bio()
                )
        );

        return "admin-consultant-edit";
    }

    @PostMapping("/admin/consultants/{id}/edit")
    public String updateConsultant(
            @PathVariable Long id,
            @Valid @ModelAttribute("consultantForm")
            CreateConsultantRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        consultantService.getConsultantById(id);

        if (bindingResult.hasErrors()) {
            model.addAttribute("consultantId", id);
            return "admin-consultant-edit";
        }

        consultantService.updateConsultant(id, request);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Đã cập nhật tư vấn viên."
        );

        return "redirect:/admin/consultants";
    }
}