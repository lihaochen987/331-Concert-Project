package proj.concert.service.domain;

import javax.persistence.*;

@Entity(name = "ConcertSummary")
@Table(name = "CONCERTSUMMARY")
public class ConcertSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "IMAGENAME")
    private String imageName;

    public ConcertSummary() {
    }

    public ConcertSummary(Long id, String title, String imageName) {
        this.title = title;
        this.imageName = imageName;
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getImageName() {
        return imageName;
    }
}
