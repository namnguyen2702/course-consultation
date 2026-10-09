package vn.coursebooking.booking;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(
            Principal principal,
            @Valid @RequestBody CreateBookingRequest request
    ) {
        Booking booking = bookingService.createBooking(
                principal.getName(), request
        );

        return ResponseEntity.status(201).body(booking);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(
            Principal principal,
            @PathVariable Long id
    ) {
        Booking booking = bookingService.cancelBooking(principal.getName(), id);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/mine")
    public List<Booking> getMyBookings(Principal principal) {
        return bookingService.getMyBookings(principal.getName());
    }
}