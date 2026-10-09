package vn.coursebooking.consultant;

import jakarta.persistence.*;

@Entity
@Table(name = "consultants")
public class ConsultantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, length = 200)
    private String expertise;

    @Column(columnDefinition = "text")
    private String bio;

    @Column(nullable = false)
    private boolean active = true;

    protected ConsultantEntity() {
    }

    public ConsultantEntity(String fullName, String expertise, String bio) {
        this.fullName = fullName;
        this.expertise = expertise;
        this.bio = bio;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getExpertise() {
        return expertise;
    }

    public String getBio() {
        return bio;
    }
    public void updateInfo(
            String fullName,
            String expertise,
            String bio
    ) {
        this.fullName = fullName;
        this.expertise = expertise;
        this.bio = bio;
    }
}