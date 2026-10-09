package vn.coursebooking.web;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.coursebooking.consultant.ConsultantWorkspaceService;
import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ConsultantWorkspacePageController {
    private final ConsultantWorkspaceService workspace;
    public ConsultantWorkspacePageController(ConsultantWorkspaceService workspace) { this.workspace = workspace; }
    @GetMapping("/consultant/slots")
    public String slots(Model model, Principal principal) {
        model.addAttribute("profile", workspace.getProfile(principal.getName()));
        model.addAttribute("slots", workspace.mySlots(principal.getName()));
        return "consultant-slots";
    }
    @PostMapping("/consultant/slots")
    public String createSlot(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            Principal principal, RedirectAttributes redirect) {
        try {
            workspace.createSlot(principal.getName(), startAt);
            redirect.addFlashAttribute("successMessage", "Đã mở khung giờ nhận tư vấn.");
        } catch (ResponseStatusException exception) {
            redirect.addFlashAttribute("errorMessage", exception.getReason());
            redirect.addFlashAttribute("startAt", startAt);
        }
        return "redirect:/consultant/slots";
    }
    @PostMapping("/consultant/slots/{id}/deactivate")
    public String deactivate(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        try {
            workspace.deactivateSlot(principal.getName(), id);
            redirect.addFlashAttribute("successMessage", "Đã ngừng nhận khung giờ.");
        } catch (ResponseStatusException exception) { redirect.addFlashAttribute("errorMessage", exception.getReason()); }
        return "redirect:/consultant/slots";
    }
    @GetMapping("/consultant/bookings")
    public String bookings(Model model, Principal principal) {
        model.addAttribute("bookings", workspace.myBookings(principal.getName()));
        return "consultant-bookings";
    }
    @PostMapping("/consultant/bookings/{id}/status")
    public String finish(@PathVariable Long id, @RequestParam String status, Principal principal, RedirectAttributes redirect) {
        try {
            workspace.finishBooking(principal.getName(), id, status);
            redirect.addFlashAttribute("successMessage", "Đã ghi nhận kết quả buổi tư vấn.");
        } catch (ResponseStatusException exception) { redirect.addFlashAttribute("errorMessage", exception.getReason()); }
        return "redirect:/consultant/bookings";
    }
}
