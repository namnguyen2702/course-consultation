package booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import vn.coursebooking.booking.BookingRepository;
import vn.coursebooking.booking.BookingService;
import vn.coursebooking.booking.CreateBookingRequest;
import vn.coursebooking.consultant.ConsultantRepository;
import vn.coursebooking.course.CourseEntity;
import vn.coursebooking.course.CourseRepository;
import vn.coursebooking.slot.ConsultationSlotEntity;
import vn.coursebooking.slot.ConsultationSlotRepository;
import vn.coursebooking.user.UserEntity;
import vn.coursebooking.user.UserRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ConsultationSlotRepository slotRepository;

    @Mock
    private ConsultantRepository consultantRepository;

    private BookingService bookingService;
    private UserEntity customer;
    private LocalDateTime startAt;

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(
                bookingRepository,
                userRepository,
                courseRepository,
                slotRepository,
                consultantRepository
        );

        customer = mock(UserEntity.class);
        when(customer.getRole()).thenReturn("CUSTOMER");

        when(userRepository.findByEmailForUpdate("an@example.com"))
                .thenReturn(Optional.of(customer));

        when(courseRepository.findByIdAndActiveTrue(1L))
                .thenReturn(Optional.of(mock(CourseEntity.class)));

        startAt = LocalDateTime.now(
                        ZoneId.of("Asia/Ho_Chi_Minh")
                ).plusDays(7).withHour(9).withMinute(0)
                .withSecond(0).withNano(0);

        ConsultationSlotEntity slot =
                mock(ConsultationSlotEntity.class);

        when(slot.getId()).thenReturn(10L);
        when(slot.isActive()).thenReturn(true);
        when(slot.getStartAt()).thenReturn(startAt);

        when(slotRepository.findForUpdate(10L))
                .thenReturn(Optional.of(slot));
    }

    @Test
    void rejectsSlotAlreadyBooked() {
        when(bookingRepository.existsBySlotIdAndStatusNot(
                10L, "CANCELLED"
        )).thenReturn(true);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> bookingService.createBooking(
                        "an@example.com",
                        new CreateBookingRequest(1L, 10L)
                )
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());
        assertEquals("Khung giờ đã được đặt", error.getReason());

        verify(bookingRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsCustomerWithBookingAtSameTime() {
        when(customer.getId()).thenReturn(1L);

        when(bookingRepository.countBookingsAtTime(
                1L, startAt
        )).thenReturn(1L);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> bookingService.createBooking(
                        "an@example.com",
                        new CreateBookingRequest(1L, 10L)
                )
        );

        assertEquals(HttpStatus.CONFLICT, error.getStatusCode());

        assertEquals(
                "Bạn đã có lịch tư vấn vào giờ này",
                error.getReason()
        );

        verify(bookingRepository, never()).saveAndFlush(any());
    }
}