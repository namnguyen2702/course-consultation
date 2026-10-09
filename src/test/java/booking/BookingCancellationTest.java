package booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import vn.coursebooking.booking.BookingEntity;
import vn.coursebooking.booking.BookingRepository;
import vn.coursebooking.booking.BookingService;
import vn.coursebooking.consultant.ConsultantRepository;
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
class BookingCancellationTest {

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

    @BeforeEach
    void setUp() {
        bookingService = new BookingService(
                bookingRepository,
                userRepository,
                courseRepository,
                slotRepository,
                consultantRepository
        );

        UserEntity customer = mock(UserEntity.class);

        when(customer.getId()).thenReturn(1L);

        when(userRepository.findByEmail("an@example.com"))
                .thenReturn(Optional.of(customer));
    }

    @Test
    void rejectsBookingNotOwnedByCustomer() {
        when(bookingRepository.findByIdAndUserId(20L, 1L))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> bookingService.cancelBooking(
                        "an@example.com", 20L
                )
        );

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());

        assertEquals(
                "Không tìm thấy lịch tư vấn của bạn",
                error.getReason()
        );

        verify(bookingRepository, never()).saveAndFlush(any());
    }

    @Test
    void rejectsCancellationLessThanTwoHoursBeforeStart() {
        BookingEntity booking = mock(BookingEntity.class);

        when(booking.getStatus()).thenReturn("CONFIRMED");
        when(booking.getSlotId()).thenReturn(10L);

        when(bookingRepository.findByIdAndUserId(20L, 1L))
                .thenReturn(Optional.of(booking));

        LocalDateTime startAt = LocalDateTime.now(
                ZoneId.of("Asia/Ho_Chi_Minh")
        ).plusHours(1);

        ConsultationSlotEntity slot = mock(ConsultationSlotEntity.class);

        when(slot.getStartAt()).thenReturn(startAt);

        when(slotRepository.findById(10L))
                .thenReturn(Optional.of(slot));

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> bookingService.cancelBooking(
                        "an@example.com", 20L
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());

        assertEquals(
                "Chỉ được hủy trước giờ tư vấn ít nhất 2 giờ",
                error.getReason()
        );

        verify(booking, never()).cancel();
        verify(bookingRepository, never()).saveAndFlush(any());
    }
}