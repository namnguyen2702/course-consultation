package vn.coursebooking.booking;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import vn.coursebooking.course.CourseRepository;
import vn.coursebooking.slot.ConsultationSlotEntity;
import vn.coursebooking.slot.ConsultationSlotRepository;
import vn.coursebooking.user.UserEntity;
import vn.coursebooking.user.UserRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import vn.coursebooking.consultant.ConsultantRepository;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ConsultationSlotRepository slotRepository;
    private final ConsultantRepository consultantRepository;

    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            CourseRepository courseRepository,
            ConsultationSlotRepository slotRepository, ConsultantRepository consultantRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.slotRepository = slotRepository;
        this.consultantRepository = consultantRepository;
    }

    public Booking createBooking(
            String email,
            CreateBookingRequest request
    ) {
        UserEntity user = getUser(email);

        if (!"CUSTOMER".equals(user.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Chỉ tài khoản khách hàng được đặt lịch"
            );
        }

        courseRepository.findByIdAndActiveTrue(request.courseId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy khóa học đang hoạt động"
                ));

        ConsultationSlotEntity slot =
                slotRepository.findById(request.slotId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Không tìm thấy khung giờ"
                        ));

        if (!slot.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Khung giờ đã ngừng nhận lịch"
            );
        }

        LocalDateTime now = LocalDateTime.now(
                ZoneId.of("Asia/Ho_Chi_Minh")
        );

        if (!slot.getStartAt().isAfter(now)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Khung giờ đã bắt đầu hoặc đã qua"
            );
        }

        if (bookingRepository.existsBySlotIdAndStatusNot(
                slot.getId(), "CANCELLED"
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Khung giờ đã được đặt"
            );
        }

        BookingEntity entity = new BookingEntity(
                user.getId(),
                request.courseId(),
                slot.getId()
        );

        try {
            BookingEntity savedEntity =
                    bookingRepository.saveAndFlush(entity);

            return toBooking(savedEntity);
        } catch (DataIntegrityViolationException exception) {
            // Hai request có thể cùng vượt qua bước kiểm tra phía trên.
            // Unique index trong PostgreSQL quyết định ai lưu thành công.
            Throwable cause = exception;

            while (cause != null) {
                if (cause instanceof ConstraintViolationException violation
                        && "uq_booking_occupied_slot".equals(
                        violation.getConstraintName()
                )) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Khung giờ vừa được người khác đặt",
                            exception
                    );
                }

                cause = cause.getCause();
            }

            throw exception;
        }
    }

    public List<Booking> getMyBookings(String email) {
        UserEntity user = getUser(email);

        List<BookingEntity> entities =
                bookingRepository.findByUserIdOrderByCreatedAtDescIdDesc(
                        user.getId()
                );

        List<Booking> bookings = new ArrayList<>();

        for (BookingEntity entity : entities) {
            bookings.add(toBooking(entity));
        }

        return bookings;
    }

    @org.springframework.transaction.annotation.Transactional
    public Booking cancelBooking(String email, Long bookingId) {
        UserEntity user = getUser(email);

        // Tim theo ca ID va chu so huu de khong sua lich cua nguoi khac.
        BookingEntity booking = bookingRepository
                .findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy lịch tư vấn của bạn"
                ));

        if ("CANCELLED".equals(booking.getStatus())) {
            return toBooking(booking);
        }

        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Trạng thái hiện tại không cho phép hủy"
            );
        }

        ConsultationSlotEntity slot = slotRepository.findById(booking.getSlotId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Không tìm thấy khung giờ của booking"
                ));

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDateTime deadline = slot.getStartAt().minusHours(2);

        if (now.isAfter(deadline)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Chỉ được hủy trước giờ tư vấn ít nhất 2 giờ"
            );
        }

        booking.cancel();
        bookingRepository.saveAndFlush(booking);
        return toBooking(booking);
    }

    public List<AdminBooking> getAllBookings() {
        List<AdminBooking> bookings = new ArrayList<>();
        for (BookingEntity entity : bookingRepository.findAllByOrderByCreatedAtDescIdDesc()) {
            bookings.add(toAdminBooking(entity));
        }
        return bookings;
    }

    @org.springframework.transaction.annotation.Transactional
    public AdminBooking finishBooking(Long id, String status) {
        if (!"COMPLETED".equals(status) && !"NO_SHOW".equals(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Chỉ chấp nhận COMPLETED hoặc NO_SHOW");
        }
        BookingEntity booking = bookingRepository.findForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy booking"));
        if (status.equals(booking.getStatus())) {
            return toAdminBooking(booking);
        }
        if (!"CONFIRMED".equals(booking.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Chỉ được cập nhật booking đang CONFIRMED");
        }
        ConsultationSlotEntity slot = slotRepository.findById(booking.getSlotId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Không tìm thấy khung giờ"));
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        if (now.isBefore(slot.getStartAt().plusMinutes(30))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Chỉ cập nhật kết quả sau khi buổi tư vấn kết thúc");
        }
        booking.finish(status);
        bookingRepository.saveAndFlush(booking);
        return toAdminBooking(booking);
    }

    private AdminBooking toAdminBooking(BookingEntity entity) {
        UserEntity user = userRepository.findById(entity.getUserId()).orElseThrow();
        ConsultationSlotEntity slot = slotRepository.findById(entity.getSlotId()).orElseThrow();
        return new AdminBooking(entity.getId(), user.getFullName(), user.getEmail(),
                entity.getCourseId(), slot.getConsultantId(), entity.getSlotId(),
                slot.getStartAt(), slot.getStartAt().plusMinutes(30),
                entity.getStatus(), entity.getCreatedAt());
    }

    private UserEntity getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Tài khoản không còn tồn tại"
                ));
    }

    private Booking toBooking(BookingEntity entity) {
        return new Booking(
                entity.getId(),
                entity.getCourseId(),
                entity.getSlotId(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
    public List<MyBooking> getMyBookingDetails(String email) {
        UserEntity user = getUser(email);

        List<BookingEntity> entities =
                bookingRepository.findByUserIdOrderByCreatedAtDescIdDesc(
                        user.getId()
                );

        List<MyBooking> results = new ArrayList<>();

        for (BookingEntity entity : entities) {
            var course = courseRepository
                    .findById(entity.getCourseId())
                    .orElseThrow();

            var slot = slotRepository
                    .findById(entity.getSlotId())
                    .orElseThrow();

            var consultant = consultantRepository
                    .findById(slot.getConsultantId())
                    .orElseThrow();

            MyBooking item = new MyBooking(
                    entity.getId(),
                    course.getName(),
                    consultant.getFullName(),
                    slot.getStartAt(),
                    slot.getStartAt().plusMinutes(30),
                    entity.getStatus()
            );

            results.add(item);
        }

        return results;
    }
}