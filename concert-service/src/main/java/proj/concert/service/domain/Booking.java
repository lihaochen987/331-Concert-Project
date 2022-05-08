package proj.concert.service.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import proj.concert.common.jackson.LocalDateTimeDeserializer;
import proj.concert.common.jackson.LocalDateTimeSerializer;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a completed booking.
 * id          the unique identifier for the booking
 * concertId   the id of the concert which was booked
 * date        the date on which that concert was booked
 * seats       the seats which were booked for that concert on that date
 * user        the user associated with the booking
 */
@Entity(name = "BOOKING")
@Table(name = "BOOKINGS")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private long id;

    @Column(name = "CONCERTID", nullable = false)
    private long concertId;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Column(name = "DATE")
    private LocalDateTime date;

    @OneToMany(orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "SEATS")
    private List<Seat> seats = new ArrayList<>();

    @ManyToOne (fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_ID")
    private User user;

    public Booking() {
    }

    public Booking(long concertId, LocalDateTime date, List<Seat> seats) {
        this.concertId = concertId;
        this.date = date;
        this.seats = seats;
    }

    public long getConcertId() {
        return concertId;
    }

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
