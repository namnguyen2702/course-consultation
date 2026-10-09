package vn.coursebooking.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import vn.coursebooking.booking.BookingService;
import vn.coursebooking.booking.CreateBookingRequest;
import vn.coursebooking.course.CourseService;
import vn.coursebooking.user.UserRepository;
import java.security.Principal;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import vn.coursebooking.course.Course;
import vn.coursebooking.consultant.ConsultantService;
import vn.coursebooking.slot.ConsultationSlotService;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {
    private final CourseService courseService;
    private final UserRepository userRepository;
    private final ConsultantService consultantService;
    private final ConsultationSlotService slotService;
    private final BookingService bookingService;

    public PageController(
            CourseService courseService,
            UserRepository userRepository,
            ConsultantService consultantService,
            ConsultationSlotService slotService,
            BookingService bookingService
    ) {
        this.courseService = courseService;
        this.userRepository = userRepository;
        this.consultantService = consultantService;
        this.slotService = slotService;
        this.bookingService = bookingService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/courses";
    }

    @GetMapping("/courses")
    public String courses(Model model, Principal principal) {
        model.addAttribute("courses", courseService.getAllCourses());
        if (principal != null) {
            userRepository.findByEmail(principal.getName()).ifPresent(user -> {
                model.addAttribute("displayName", user.getFullName());
                model.addAttribute("role", user.getRole());
            });
        }
        return "courses";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/booking")
    public String booking(
            @RequestParam Long courseId,
            @RequestParam(required = false) Long consultantId,
            Model model
    ) {
        Course course = courseService.getCourseById(courseId);

        if (course == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy khóa học"
            );
        }

        model.addAttribute("course", course);
        model.addAttribute(
                "consultants",
                consultantService.getAllConsultants()
        );

        model.addAttribute("selectedConsultantId", consultantId);

        if (consultantId != null) {
            model.addAttribute(
                    "slots",
                    slotService.getSlots(consultantId)
            );
        }

        return "booking";
    }
    @PostMapping("/booking")
    public String submitBooking(
            @RequestParam Long courseId,
            @RequestParam Long slotId,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (courseId <= 0 || slotId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "ID khóa học và khung giờ phải lớn hơn 0"
            );
        }

        CreateBookingRequest request =
                new CreateBookingRequest(courseId, slotId);

        try {
            bookingService.createBooking(
                    principal.getName(),
                    request
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đặt lịch tư vấn thành công!"
            );

            return "redirect:/courses";

        } catch (ResponseStatusException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getReason()
            );

            redirectAttributes.addAttribute("courseId", courseId);

            return "redirect:/booking";
        }
    }
    @GetMapping("/my-bookings")
    public String myBookings(
            Principal principal,
            Model model
    ) {
        model.addAttribute(
                "bookings",
                bookingService.getMyBookingDetails(
                        principal.getName()
                )
        );

        return "my-bookings";
    }
    @PostMapping("/my-bookings/{id}/cancel")
    public String cancelMyBooking(
            @PathVariable Long id,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            bookingService.cancelBooking(
                    principal.getName(),
                    id
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã hủy lịch tư vấn."
            );

        } catch (ResponseStatusException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getReason()
            );
        }

        return "redirect:/my-bookings";
    }
}