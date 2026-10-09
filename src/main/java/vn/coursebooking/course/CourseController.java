package vn.coursebooking.course;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.List;
import org.springframework.web.bind.annotation.PatchMapping;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService courseService;

    // Spring tự truyền CourseService vào constructor (dependency injection).
    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    public List<Course> getAllCourses() {
        return courseService.getAllCourses();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        if (course == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(course);
    }
    @PostMapping
    public ResponseEntity<Course> createCourse(
            @Valid @RequestBody CreateCourseRequest request
    ) {
        Course createdCourse = courseService.createCourse(request);

        return ResponseEntity.status(201).body(createdCourse);
    }
    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CreateCourseRequest request
    ) {
        Course updatedCourse = courseService.updateCourse(id, request);

        if (updatedCourse == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedCourse);
    }
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCourse(@PathVariable Long id) {
        boolean found = courseService.deactivateCourse(id);

        if (!found) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
