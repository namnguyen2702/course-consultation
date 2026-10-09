package vn.coursebooking.consultant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.coursebooking.application.ConsultantApplicationService;
import vn.coursebooking.user.UserRepository;
import vn.coursebooking.slot.*;
import vn.coursebooking.booking.*;
import vn.coursebooking.course.CourseRepository;

import java.time.*;
import java.util.*;

@Service
public class ConsultantWorkspaceService {
    private final UserRepository users;
    private final ConsultantRepository consultants;
    private final ConsultationSlotRepository slots;
    private final ConsultationSlotService slotService;
    private final BookingRepository bookings;
    private final BookingService bookingService;
    private final CourseRepository courses;

    public ConsultantWorkspaceService(UserRepository users, ConsultantRepository consultants,
                                      ConsultationSlotRepository slots, ConsultationSlotService slotService,
                                      BookingRepository bookings, BookingService bookingService, CourseRepository courses) {
        this.users = users;
        this.consultants = consultants;
        this.slots = slots;
        this.slotService = slotService;
        this.bookings = bookings;
        this.bookingService = bookingService;
        this.courses = courses;
    }

    private ConsultantEntity profile(String email) {
        var user = users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        if (!"CONSULTANT".equals(user.getRole())) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return consultants.findByUserId(user.getId()).filter(ConsultantEntity::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Hồ sơ tư vấn viên chưa được duyệt hoặc đã ngừng hoạt động"));
    }

    public record ProfileView(String fullName, String days, LocalTime from, LocalTime until, List<String> courseNames) {
    }

    public record SlotView(Long id, LocalDateTime startAt, LocalDateTime endAt, boolean active, boolean booked) {
    }

    public record BookingView(Long id, String customerName, String customerEmail, String courseName,
                              LocalDateTime startAt, String status) {
    }

    @Transactional(readOnly = true)
    public ProfileView getProfile(String email) {
        var profile = profile(email);
        return new ProfileView(profile.getFullName(), ConsultantApplicationService.daysLabel(profile.getWorkDays()),
                profile.getAvailableFrom(), profile.getAvailableUntil(), profile.getCourseIds().stream().sorted()
                .map(id -> courses.findById(id).map(c -> c.getName()).orElse("Khóa học #" + id)).toList());
    }

    @Transactional(readOnly = true)
    public List<SlotView> mySlots(String email) {
        var profile = profile(email);
        return slots.findByConsultantIdAndStartAtAfterOrderByStartAtAsc(profile.getId(), LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")))
                .stream().map(slot -> new SlotView(slot.getId(), slot.getStartAt(), slot.getStartAt().plusMinutes(30),
                        slot.isActive(), bookings.existsBySlotIdAndStatusNot(slot.getId(), "CANCELLED"))).toList();
    }

    @Transactional
    public void createSlot(String email, LocalDateTime startAt) {
        var profile = profile(email);
        slotService.createSlot(new CreateSlotRequest(profile.getId(), startAt));
    }

    @Transactional
    public void deactivateSlot(String email, Long slotId) {
        var profile = profile(email);
        var slot = slots.findForUpdate(slotId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!profile.getId().equals(slot.getConsultantId())) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        slotService.deactivateSlot(slotId);
    }

    @Transactional(readOnly = true)
    public List<BookingView> myBookings(String email) {
        var profile = profile(email);
        return bookings.findForConsultant(profile.getId()).stream().map(booking -> {
            var customer = users.findById(booking.getUserId()).orElseThrow();
            var slot = slots.findById(booking.getSlotId()).orElseThrow();
            var course = courses.findById(booking.getCourseId()).orElseThrow();
            return new BookingView(booking.getId(), customer.getFullName(), customer.getEmail(),
                    course.getName(), slot.getStartAt(), booking.getStatus());
        }).toList();
    }

    @Transactional
    public void finishBooking(String email, Long bookingId, String status) {
        var profile = profile(email);
        var booking = bookings.findForUpdate(bookingId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        var slot = slots.findById(booking.getSlotId()).orElseThrow();
        if (!profile.getId().equals(slot.getConsultantId())) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        bookingService.finishBooking(bookingId, status);
    }
}
