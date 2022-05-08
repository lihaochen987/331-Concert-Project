package proj.concert.service.util;

import proj.concert.common.dto.BookingRequestDTO;
import proj.concert.common.dto.PerformerDTO;
import proj.concert.common.dto.UserDTO;
import proj.concert.common.types.BookingStatus;
import proj.concert.service.domain.*;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hibernate.tool.schema.SchemaToolingLogging.LOGGER;

/**
 * Utility class that provides functions for ConcertResource. Stores a majority of the logic in the code and
 * allows ConcertResource to be more readable and meaningful.
 */
public class ConcertResourceUtils {

    // User helper functions
    public static void findUserAndAssignToken(EntityManager em, UserDTO dtoUser, NewCookie cookie) {
        User domainUser = new User(dtoUser.getUsername(), dtoUser.getPassword());
        User user;
        try {
            user = em
                    .createQuery("select u from User u where u.username=:username and u.password=:password", User.class)
                    .setParameter("username", domainUser.getUsername())
                    .setParameter("password", domainUser.getPassword())
                    .getSingleResult();
            user.setToken(cookie.getValue());
            em.merge(user);
        } catch (NoResultException e) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
    }

    public static void checkAuthenticationNotNull(Cookie auth) {
        if (auth == null) {
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
    }

    public static User authenticate(EntityManager em, Cookie cookie) throws Exception {
        LOGGER.info("Searching cookie " + cookie.getValue());
        String token = cookie.getValue();
        User user;
        TypedQuery<User> query = em
                .createQuery("select u from User u where u.token=:token", User.class)
                .setParameter("token", token);

        if (query.getResultList().isEmpty()) {
            throw new Exception();
        }

        user = query.getSingleResult();
        return user;
    }

    // Concert helper functions
    public static Concert findConcert(EntityManager em, Long id, String method) {
        Concert concert = em.find(Concert.class, id);
        if (concert == null) {
            entityExceptionDecisionManager(method);
        }
        return concert;
    }

    public static List<Concert> findAllConcerts(EntityManager em) {
        return em
                .createQuery("select c from Concert c", Concert.class)
                .getResultList();
    }

    // Performer helper functions
    public static PerformerDTO getDtoPerformer(EntityManager em, Long id, String method) {
        Performer performer = em.find(Performer.class, id);
        if (performer == null) {
            entityExceptionDecisionManager(method);
        }
        return new PerformerDTO(performer.getId(), performer.getName(), performer.getImageName(), performer.getGenre(), performer.getBlurb());
    }

    public static ArrayList<PerformerDTO> getAllDtoPerformers(EntityManager em) {
        ArrayList<PerformerDTO> dtoPerformers = new ArrayList<PerformerDTO>();
        List<Performer> performers = em
                .createQuery("select p from Performer p", Performer.class)
                .getResultList();

        for (Performer performer : performers) {
            dtoPerformers.add(new PerformerDTO(performer.getId(), performer.getName(), performer.getImageName(), performer.getGenre(), performer.getBlurb()));
        }
        return dtoPerformers;
    }

    // Seat helper functions
    public static ArrayList<Seat> findSeats(EntityManager em, BookingRequestDTO bReq) {
        ArrayList<Seat> seats = new ArrayList<Seat>();
        for (String seatLabel : bReq.getSeatLabels()) {
            TypedQuery<Seat> seat = em
                    .createQuery("select s from Seat s where s.label=:label and s.date=:date", Seat.class)
                    .setLockMode(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
                    .setParameter("label", seatLabel)
                    .setParameter("date", bReq.getDate());

            if (seat.getResultList().isEmpty()) {
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
            }

            if (seat.getSingleResult().getBookingStatus()) {
                throw new WebApplicationException(Response.Status.FORBIDDEN);
            }

            seat.getSingleResult().setBookingStatus(true);
            seats.add(seat.getSingleResult());
        }
        return seats;
    }

    public static double getAvailableSeats(EntityManager em, Booking booking) {
        List<Seat> availableSeatsQuery = em
                .createQuery("select s from Seat s where s.date=:date and s.isBooked=:status", Seat.class)
                .setParameter("date", booking.getDate())
                .setParameter("status", false)
                .getResultList();
        return availableSeatsQuery.size();
    }

    public static double getTotalSeats(EntityManager em, Booking booking) {
        List<Seat> totalSeatsQuery = em
                .createQuery("select s from Seat s where s.date=:date", Seat.class)
                .setParameter("date", booking.getDate())
                .getResultList();
        return totalSeatsQuery.size();
    }

    // Utility functions
    public static void entityExceptionDecisionManager(String method) {
        switch (method) {
            case "GET":
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            case "POST":
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    public static TypedQuery<Seat> seatStatusDecisionManager(EntityManager em, BookingStatus bookingStatus, LocalDateTime date) {
        switch (bookingStatus) {
            case Booked:
                return em
                        .createQuery("select s from Seat s where s.date=:date and s.isBooked=:status", Seat.class)
                        .setParameter("date", date)
                        .setParameter("status", true);
            case Unbooked:
                return em
                        .createQuery("select s from Seat s where s.date=:date and s.isBooked=:status", Seat.class)
                        .setParameter("date", date)
                        .setParameter("status", false);
            default:
                return em
                        .createQuery("select s from Seat s where s.date=:date", Seat.class)
                        .setParameter("date", date);
        }
    }
}
