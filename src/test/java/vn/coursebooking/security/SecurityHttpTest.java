package vn.coursebooking.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import vn.coursebooking.booking.AdminBookingController;
import vn.coursebooking.booking.Booking;
import vn.coursebooking.booking.BookingController;
import vn.coursebooking.booking.BookingService;
import vn.coursebooking.web.AdminPageController;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {
        BookingController.class,
        AdminBookingController.class,
        AdminPageController.class
})
@Import(SecurityConfig.class)
class SecurityHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @Test
    void anonymousCannotCreateBooking() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"courseId": 1, "slotId": 10}
                            """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(bookingService);
    }

    @Test
    void customerCannotAccessAdminPage() throws Exception {
        mockMvc.perform(get("/admin/bookings")
                        .with(user("an@example.com").roles("CUSTOMER"))
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookingService);
    }

    @Test
    void customerCannotAccessAdminApi() throws Exception {
        mockMvc.perform(get("/api/admin/bookings")
                        .with(user("an@example.com").roles("CUSTOMER")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookingService);
    }

    @Test
    void adminCanAccessAdminApi() throws Exception {
        when(bookingService.getAllBookings()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/bookings")
                        .with(user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(bookingService).getAllBookings();
    }

    @Test
    void customerCannotCreateBookingWithoutCsrf() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .with(user("an@example.com").roles("CUSTOMER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"courseId": 1, "slotId": 10}
                            """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(bookingService);
    }

    @Test
    void customerWithCsrfCanReachBookingService() throws Exception {
        Booking result = new Booking(
                20L,
                1L,
                10L,
                "CONFIRMED",
                LocalDateTime.of(2026, 10, 10, 9, 0)
        );

        when(bookingService.createBooking(
                eq("an@example.com"), any()
        )).thenReturn(result);

        mockMvc.perform(post("/api/bookings")
                        .with(user("an@example.com").roles("CUSTOMER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"courseId": 1, "slotId": 10}
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(bookingService).createBooking(
                eq("an@example.com"), any()
        );
    }
}