package vn.coursebooking.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.coursebooking.booking.BookingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminPageController {

    private final BookingService bookingService;

    public AdminPageController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/admin/bookings")
    public String bookings(Model model) {
        model.addAttribute(
                "bookings",
                bookingService.getAllBookings()
        );

        return "admin-bookings";
    }
    @PostMapping("admin/bookings/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes
    ){
        try {
            bookingService.finishBooking(id, status);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã cập nhật kết quả buổi phỏng vấn."
            );

        } catch (ResponseStatusException exception){
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getReason()
            );
        }
        return "redirect:/admin/bookings";
    }
}