package vn.coursebooking.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.coursebooking.application.*;
import vn.coursebooking.course.CourseService;
import vn.coursebooking.user.UserRepository;
import java.security.Principal;

@Controller
public class ConsultantApplicationPageController {
    private final ConsultantApplicationService applications;
    private final CourseService courses;
    private final UserRepository users;
    public ConsultantApplicationPageController(ConsultantApplicationService applications, CourseService courses, UserRepository users) {
        this.applications = applications; this.courses = courses; this.users = users;
    }
    private void populate(Model model, Principal principal) {
        model.addAttribute("registration", applications.latest(principal.getName()));
        model.addAttribute("courses", courses.getAllCourses());
        model.addAttribute("user", users.findByEmail(principal.getName()).orElseThrow());
    }
    @GetMapping("/consultant-application")
    public String application(Model model, Principal principal) {
        populate(model, principal);
        model.addAttribute("applicationForm", ConsultantApplicationRequest.empty());
        return "consultant-application";
    }
    @PostMapping("/consultant-application")
    public String submit(@Valid @ModelAttribute("applicationForm") ConsultantApplicationRequest request,
            BindingResult result, Model model, Principal principal, RedirectAttributes redirect) {
        if (!result.hasErrors()) {
            try {
                applications.submit(principal.getName(), request);
                redirect.addFlashAttribute("successMessage", "Đã gửi hồ sơ. Bạn có thể theo dõi kết quả tại đây.");
                return "redirect:/consultant-application";
            } catch (ResponseStatusException exception) { model.addAttribute("errorMessage", exception.getReason()); }
        }
        populate(model, principal);
        return "consultant-application";
    }
    @GetMapping("/admin/applications")
    public String reviewPage(Model model) {
        model.addAttribute("applications", applications.all());
        return "admin-applications";
    }
    @PostMapping("/admin/applications/{id}/review")
    public String review(@PathVariable Long id, @RequestParam String decision,
            @RequestParam(defaultValue = "") String note, Principal principal, RedirectAttributes redirect) {
        try {
            applications.review(id, principal.getName(), decision, note);
            redirect.addFlashAttribute("successMessage", "Đã xử lý hồ sơ đăng ký.");
        } catch (ResponseStatusException exception) { redirect.addFlashAttribute("errorMessage", exception.getReason()); }
        return "redirect:/admin/applications";
    }
}
