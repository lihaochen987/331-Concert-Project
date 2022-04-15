package proj.concert.service.domain;

import java.time.LocalDateTime;
import java.util.*;

import javax.persistence.*;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import proj.concert.common.dto.PerformerDTO;

/**
 * DTO class to represent concerts.
 * <p>
 * A ConcertDTO describes a concert in terms of
 * id           the unique identifier for a concert.
 * title        the concert's title.
 * dates        the concert's scheduled dates and times (represented as a Set of LocalDateTime instances).
 * imageName    an image name for the concert.
 * performers   the performers in the concert
 * blurb        the concert's description
 */

@Entity
@Table(name = "CONCERT")
public class Concert implements Comparable<Concert> {
    // TODO Implement this class.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "TITLE")
    private String title;

    @Column(name = "IMAGENAME")
    private String imageName;

    @Column(name = "BLRB")
    private String blrb;

    private List<LocalDateTime> dates = new ArrayList<>();

    private List<Performer> performers = new ArrayList<>();

    public Concert() {
    }

    public Concert(String title, String imageName, String blrb, List<Performer> performers, List<LocalDateTime> dates) {
        this.title = title;
        this.imageName = imageName;
        this.blrb = blrb;
        this.performers = performers;
        this.dates = dates;
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

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getBlrb() {
        return blrb;
    }

    public void setBlrb(String blrb) {
        this.blrb = blrb;
    }

    public Set<LocalDateTime> getDates() {
        return null;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Concert, id: ");
        buffer.append(id);
        buffer.append(", title: ");
        buffer.append(title);
        buffer.append(", imageName: ");
        buffer.append(imageName);
        buffer.append(", blrb: ");
        buffer.append(blrb);
        //TODO Add dates and performers for testing purposes

        return buffer.toString();
    }

    @Override
    public boolean equals(Object obj) {
        // Implement value-equality based on a Concert's title alone. ID isn't
        // included in the equality check because two Concert objects could
        // represent the same real-world Concert, where one is stored in the
        // database (and therefore has an ID - a primary key) and the other
        // doesn't (it exists only in memory).
        if (!(obj instanceof Concert))
            return false;
        if (obj == this)
            return true;

        Concert rhs = (Concert) obj;
        return new EqualsBuilder().
                append(title, rhs.title).
                isEquals();
    }

    @Override
    public int hashCode() {
        // Hash-code value is derived from the value of the title field. It's
        // good practice for the hash code to be generated based on a value
        // that doesn't change.
        return new HashCodeBuilder(17, 31).
                append(title).hashCode();
    }

    @Override
    public int compareTo(Concert concert) {
        return title.compareTo(concert.getTitle());
    }
}
