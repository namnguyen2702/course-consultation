package vn.coursebooking.course;
import jakarta.persistence.*;
@Entity
@Table(name = "courses")
public class CourseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column (nullable = false, length = 150)
    private String name;
    @Column (columnDefinition = "text")
    private String description;
    @Column(name = "prerequisites", columnDefinition = "text")
    private String prerequisite;
    @Column(name = "duration_weeks", nullable = false)
    private int durationWeeks;
    @Column(nullable = false)
    private boolean active = true;
    protected CourseEntity() {
        // JPA cần constructor không tham số để tạo đối tượng khi đọc dữ liệu.
    }
    public CourseEntity(
            String name,
            String description,
            String prerequisites,
            int durationWeeks
    ) {
        this.name = name;
        this.description = description;
        this.prerequisite = prerequisites;
        this.durationWeeks = durationWeeks;
    }
    public void updateInfo(
            String name,
            String description,
            String prerequisites,
            int durationWeeks
    ) {
        this.name = name;
        this.description = description;
        this.prerequisite = prerequisites;
        this.durationWeeks = durationWeeks;
    }
    public Long getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public String getDescription(){
        return description;
    }
    public String getPrerequisite(){
        return prerequisite;
    }
    public int getDurationWeeks(){
        return durationWeeks;
    }
    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }
}
