package vn.coursebooking.booking;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/bookings")
public class AdminBookingController {
    private final BookingService bookingService;

    public AdminBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<AdminBooking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @PatchMapping("/{id}/status")
    public AdminBooking updateStatus(@PathVariable Long id,
            @Valid @RequestBody UpdateBookingStatusRequest request) {
        return bookingService.finishBooking(id, request.status());
    }
}