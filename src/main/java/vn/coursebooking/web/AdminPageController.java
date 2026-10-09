package vn.coursebooking.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.coursebooking.booking.BookingService;

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
}