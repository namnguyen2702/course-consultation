package vn.coursebooking.web;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
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
        model.addAttribute("course", courseService.getAllCourses());

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
                    "Đã thêm khoóa học."
            );
            return "admin-courses";
    }
}
