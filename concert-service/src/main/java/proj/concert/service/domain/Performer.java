package proj.concert.service.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import proj.concert.common.types.Genre;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A Performer describes a performer in terms of:
 * id         the unique identifier for a performer
 * name       the performer's name
 * imageName  the name of an image file for the performer
 * genre      the performer's genre
 * blurb      the performer's description
 * concerts   the concert the performer is in
 */
@Entity (name = "Performer")
@Table(name = "PERFORMERS")
public class Performer implements Comparable<Performer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "IMAGE_NAME")
    private String imageName;

    @Enumerated (EnumType.STRING)
    private Genre genre;

    @Column(name = "BLURB", length = 1020)
    private String blurb;

    @ManyToMany(mappedBy = "performers")
    private List<Concert> concerts = new ArrayList<>();

    public Performer() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageName() {
        return imageName;
    }

    public Genre getGenre() {
        return genre;
    }

    public String getBlurb() {
        return blurb;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || getClass() != obj.getClass()) return false;

        Performer other = (Performer) obj;

        return new EqualsBuilder()
                .append(id, other.id)
                .append(name, other.name)
                .append(imageName, other.imageName)
                .append(genre, other.genre)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(name)
                .append(imageName)
                .append(genre)
                .toHashCode();
    }

    @Override
    public int compareTo(Performer other) {
        return other.getName().compareTo(getName());
    }
}

