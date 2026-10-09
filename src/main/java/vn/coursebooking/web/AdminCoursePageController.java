package vn.coursebooking.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.coursebooking.course.CourseService;
import vn.coursebooking.course.CreateCourseRequest;

@Controller
public class AdminCoursePageController {
    private final CourseService courseService;

    public AdminCoursePageController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/admin/courses")
    public String course(Model model){
        model.addAttribute("courses", courseService.getAllCourses());

        model.addAttribute(
                "courseForm",
                new CreateCourseRequest("", "", "", 1)
        );

        return "admin-courses";
    }

    @PostMapping("/admin/courses")
    public String createCourse(
            @Valid @ModelAttribute("courseForm") CreateCourseRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
            ){
            if(bindingResult.hasErrors()){
                model.addAttribute(
                        "course",
                        courseService.getAllCourses()
                );
                return "admin-courses";
            }
        courseService.createCourse(request);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Đã thêm khóa học."
        );

        return "redirect:/admin/courses";
    }
    @GetMapping("/admin/courses/{id}/edit")
    public String editPage(
            @PathVariable Long id,
            Model model
    ) {
        var course = courseService.getCourseById(id);

        if (course == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy khóa học đang mở"
            );
        }

        model.addAttribute("courseId", id);

        model.addAttribute(
                "courseForm",
                new CreateCourseRequest(
                        course.name(),
                        course.description(),
                        course.prerequisites(),
                        course.durationWeeks()
                )
        );

        return "admin-course-edit";
    }
    @PostMapping("/admin/courses/{id}/edit")
    public String updateCourse(
            @PathVariable Long id,
            @Valid @ModelAttribute("courseForm") CreateCourseRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (courseService.getCourseById(id) == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy khóa học đang mở"
            );
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("courseId", id);
            return "admin-course-edit";
        }

        var updatedCourse = courseService.updateCourse(id, request);

        if (updatedCourse == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy khóa học"
            );
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Đã cập nhật khóa học."
        );

        return "redirect:/admin/courses";
    }
    @PostMapping("/admin/courses/{id}/deactivate")
    public String deactivateCourse(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        boolean found = courseService.deactivateCourse(id);

        if (!found) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy khóa học"
            );
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Đã ngừng mở khóa học."
        );

        return "redirect:/admin/courses";
    }
}
