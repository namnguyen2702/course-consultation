package vn.coursebooking.course;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CourseService {

    // Dữ liệu demo cố định. Bước tiếp theo sẽ đọc từ PostgreSQL qua Repository.
//    private final List<Course> courses = List.of(
//            new Course(1L, "Java cơ bản",
//                    "Học cú pháp Java, OOP và Collections.",
//                    "Không yêu cầu kinh nghiệm lập trình.", 8),
//            new Course(2L, "Java Backend với Spring Boot",
//                    "Xây dựng REST API, làm việc với SQL và triển khai backend.",
//                    "Biết Java cơ bản và OOP.", 12),
//            new Course(3L, "SQL và PostgreSQL",
//                    "Thiết kế bảng, viết truy vấn và tìm hiểu transaction.",
//                    "Có kiến thức máy tính cơ bản.", 6),
//            new Course(4L, "Git cơ bản","Quản lý mã nguồn và làm việc với GitHub.",
//                    "Không yêu cầu kinh nghiệm Git",2)
//    );
    private final CourseRepository courseRepository;
    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }
    public List<Course> getAllCourses() {
        List<CourseEntity> entities =
                courseRepository.findByActiveTrueOrderByIdAsc();

        List<Course> courses = new ArrayList<>();

        for (CourseEntity entity : entities) {
            courses.add(toCourse(entity));
        }

        return courses;
    }

    public Course getCourseById(Long id) {
        CourseEntity entity = courseRepository.findByIdAndActiveTrue(id).orElse(null);
        if(entity == null){
            return null;
        }
        return toCourse(entity);
    }
    public Course createCourse(CreateCourseRequest request){
        CourseEntity entity = new CourseEntity(
                request.name(),
                request.description(),
                request.prerequisites(),
                request.durationWeeks()
        );
        CourseEntity saveEntity = courseRepository.save(entity);
        return toCourse(saveEntity);
    }
    public Course updateCourse(Long id, CreateCourseRequest request) {
        CourseEntity entity = courseRepository.findById(id).orElse(null);

        if (entity == null) {
            return null;
        }

        entity.updateInfo(
                request.name(),
                request.description(),
                request.prerequisites(),
                request.durationWeeks()
        );

        CourseEntity savedEntity = courseRepository.save(entity);

        return toCourse(savedEntity);
    }
    private Course toCourse (CourseEntity entity){
        return new Course(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getPrerequisite(),
            entity.getDurationWeeks()
        );
    }
    public boolean deactivateCourse(Long id) {
        CourseEntity entity = courseRepository.findById(id).orElse(null);

        if (entity == null) {
            return false;
        }

        entity.deactivate();
        courseRepository.save(entity);

        return true;
    }
}
