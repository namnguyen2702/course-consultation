package vn.coursebooking.application;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.coursebooking.user.*;
import vn.coursebooking.course.CourseRepository;
import vn.coursebooking.consultant.*;
import java.net.URI;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConsultantApplicationService {
    private final ConsultantApplicationRepository applications;
    private final UserRepository users;
    private final CourseRepository courses;
    private final ConsultantRepository consultants;
    public ConsultantApplicationService(ConsultantApplicationRepository applications,
            UserRepository users, CourseRepository courses, ConsultantRepository consultants) {
        this.applications = applications; this.users = users;
        this.courses = courses; this.consultants = consultants;
    }

    @Transactional
    public void submit(String email, ConsultantApplicationRequest request) {
        UserEntity user = users.findByEmailForUpdate(email).orElseThrow(() -> failure(HttpStatus.UNAUTHORIZED, "Tài khoản không tồn tại"));
        if (!"CUSTOMER".equals(user.getRole())) throw failure(HttpStatus.FORBIDDEN, "Chỉ khách hàng được gửi đơn đăng ký");
        if (applications.existsByUserIdAndStatus(user.getId(), "PENDING"))
            throw failure(HttpStatus.CONFLICT, "Bạn đã có đơn đang chờ duyệt");
        validateHours(request.availableFrom(), request.availableUntil());
        if (request.workDays() == null || request.workDays().isEmpty()
                || request.workDays().stream().anyMatch(day -> day == null || day < 1 || day > 7))
            throw failure(HttpStatus.BAD_REQUEST, "Chọn ngày làm việc hợp lệ");
        if (request.courseIds() == null || request.courseIds().isEmpty())
            throw failure(HttpStatus.BAD_REQUEST, "Chọn ít nhất một khóa học");
        for (Long id : request.courseIds()) {
            if (id == null || courses.findByIdAndActiveTrue(id).isEmpty())
                throw failure(HttpStatus.BAD_REQUEST, "Khóa học được chọn không còn hoạt động");
        }
        if (request.portfolioUrl() != null && !request.portfolioUrl().isBlank()) {
            try {
                URI uri = URI.create(request.portfolioUrl().strip());
                if (!("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme())) || uri.getHost() == null)
                    throw new IllegalArgumentException();
            } catch (IllegalArgumentException exception) {
                throw failure(HttpStatus.BAD_REQUEST, "Đường dẫn hồ sơ phải bắt đầu bằng http:// hoặc https://");
            }
        }
        applications.saveAndFlush(new ConsultantApplicationEntity(user.getId(), request));
    }

    public static void validateHours(LocalTime from, LocalTime until) {
        if (from == null || until == null || !until.isAfter(from)
                || from.getMinute() % 30 != 0 || until.getMinute() % 30 != 0
                || from.getSecond() != 0 || until.getSecond() != 0
                || from.getNano() != 0 || until.getNano() != 0)
            throw failure(HttpStatus.BAD_REQUEST, "Giờ kết thúc phải sau giờ bắt đầu; chọn phút 00 hoặc 30, không qua đêm");
    }

    @Transactional
    public void review(Long id, String adminEmail, String decision, String note) {
        UserEntity admin = users.findByEmail(adminEmail).orElseThrow(() -> failure(HttpStatus.UNAUTHORIZED, "Tài khoản không tồn tại"));
        if (!"ADMIN".equals(admin.getRole())) throw failure(HttpStatus.FORBIDDEN, "Chỉ admin được duyệt đơn");
        if (!Set.of("APPROVED", "REJECTED").contains(decision)) throw failure(HttpStatus.BAD_REQUEST, "Quyết định không hợp lệ");
        String reviewNote = note == null ? "" : note.strip();
        if (reviewNote.length() > 500 || ("REJECTED".equals(decision) && reviewNote.isBlank()))
            throw failure(HttpStatus.BAD_REQUEST, "Khi từ chối cần ghi lý do, tối đa 500 ký tự");
        ConsultantApplicationEntity application = applications.findForUpdate(id)
                .orElseThrow(() -> failure(HttpStatus.NOT_FOUND, "Không tìm thấy đơn"));
        if (!"PENDING".equals(application.getStatus())) throw failure(HttpStatus.CONFLICT, "Đơn này đã được xử lý");
        if ("APPROVED".equals(decision)) {
            UserEntity applicant = users.findById(application.getUserId()).orElseThrow();
            if (!"CUSTOMER".equals(applicant.getRole()) || consultants.findByUserId(applicant.getId()).isPresent())
                throw failure(HttpStatus.CONFLICT, "Tài khoản đã có vai trò hoặc hồ sơ khác");
            for (Long courseId : application.getCourseIds()) {
                if (courses.findByIdAndActiveTrue(courseId).isEmpty())
                    throw failure(HttpStatus.CONFLICT, "Có khóa học đã ngừng mở. Hãy từ chối và yêu cầu đăng ký lại");
            }
            ConsultantEntity profile = new ConsultantEntity(applicant.getFullName(), application.getExpertise(), application.getBio());
            profile.linkApprovedAccount(applicant.getId(), application.getAvailableFrom(), application.getAvailableUntil(),
                    application.getWorkDays(), application.getCourseIds());
            consultants.saveAndFlush(profile);
            applicant.approveAsConsultant();
            users.save(applicant);
        }
        application.review(decision, reviewNote, admin.getId());
        applications.saveAndFlush(application);
    }

    @Transactional(readOnly = true)
    public ApplicationView latest(String email) {
        var user = users.findByEmail(email).orElseThrow();
        return applications.findFirstByUserIdOrderByIdDesc(user.getId()).map(this::view).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<ApplicationView> all() {
        return applications.findAllByOrderByCreatedAtDescIdDesc().stream().map(this::view).toList();
    }
    private ApplicationView view(ConsultantApplicationEntity a) {
        var user = users.findById(a.getUserId()).orElseThrow();
        List<String> courseNames = a.getCourseIds().stream().sorted().map(id -> courses.findById(id)
                .map(course -> course.getName()).orElse("Khóa học #" + id)).toList();
        return new ApplicationView(a.getId(), user.getFullName(), user.getEmail(), a.getPhone(), a.getExpertise(),
                a.getExperienceYears(), a.getBio(), a.getPortfolioUrl(), daysLabel(a.getWorkDays()),
                a.getAvailableFrom(), a.getAvailableUntil(), courseNames, a.getStatus(), a.getReviewNote(), a.getCreatedAt());
    }
    public static String daysLabel(Set<Integer> days) {
        return days.stream().sorted().map(day -> day == 7 ? "Chủ nhật" : "Thứ " + (day + 1)).collect(Collectors.joining(", "));
    }
    private static ResponseStatusException failure(HttpStatus status, String message) {
        return new ResponseStatusException(status, message);
    }
}
