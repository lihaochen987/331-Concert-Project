package proj.concert.service.services;

import org.h2.security.auth.Authenticator;
import proj.concert.common.dto.*;
import proj.concert.service.common.Config;
import proj.concert.service.domain.*;
import proj.concert.service.jaxrs.LocalDateTimeParam;
import proj.concert.service.mapper.*;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hibernate.tool.schema.SchemaToolingLogging.LOGGER;

@Path("/concert-service")
public class ConcertResource {
    //TODO implement this class.

    private EntityManager em = PersistenceManager.instance().createEntityManager();

    private Map<Long, Concert> concertDB;

    @GET
    @Path("/concerts/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveConcert(@PathParam("id") long id, @CookieParam("auth") Cookie auth) {
        LOGGER.info("Retrieving concert with id: " + id);
        ConcertDTO dtoConcert;
        try {
            em.getTransaction().begin();

            Concert concert = em.find(Concert.class, id);
            if (concert == null) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            dtoConcert = ConcertMapper.toDto(concert);

            em.getTransaction().commit();
        } finally {
            em.close();
        }

        return Response
                .ok(dtoConcert)
                .build();

    }

    @GET
    @Path("/concerts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveAllConcerts(@CookieParam("auth") Cookie auth) {
        LOGGER.info("Retrieving all concerts");
        ArrayList<ConcertDTO> dtoConcerts = new ArrayList<ConcertDTO>();

        try {
            em.getTransaction().begin();
            TypedQuery<Concert> query = em.createQuery("select c from Concert c", Concert.class);
            List<Concert> concerts = query.getResultList();

            for (Concert concert : concerts) {
                dtoConcerts.add(ConcertMapper.toDto(concert));
            }

            em.getTransaction().commit();
        } finally {
            em.close();
        }

        return Response
                .ok(dtoConcerts)
                .build();

    }

    @GET
    @Path("/concerts/summaries")
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveAllConcertSummaries(@CookieParam("auth") Cookie auth) {
        LOGGER.info("Retrieving all concert summaries");
        ArrayList<ConcertSummaryDTO> dtoConcertSummaries = new ArrayList<ConcertSummaryDTO>();

        try {
            em.getTransaction().begin();
            TypedQuery<Concert> query = em.createQuery("select c from Concert c", Concert.class);
            List<Concert> concerts = query.getResultList();

            for (Concert concert : concerts) {
                ConcertSummary toAdd = new ConcertSummary(concert.getId(), concert.getTitle(), concert.getImageName());
                dtoConcertSummaries.add(ConcertSummaryMapper.toDto(toAdd));
            }

            em.getTransaction().commit();
        } finally {
            em.close();
        }

        return Response
                .ok(dtoConcertSummaries)
                .build();
    }

    @GET
    @Path("/performers/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrievePerformer(@PathParam("id") long id, @CookieParam("auth") Cookie auth) {
        LOGGER.info("Retrieving performer with id: " + id);
        PerformerDTO dtoPerformer;
        try {
            em.getTransaction().begin();

            Performer performer = em.find(Performer.class, id);
            if (performer == null) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }

            dtoPerformer = PerformerMapper.toDto(performer);

            em.getTransaction().commit();
        } finally {
            em.close();
        }

        return Response
                .ok(dtoPerformer)
                .build();
    }

    @GET
    @Path("/performers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveAllPerformers(@CookieParam("auth") Cookie auth) {
        LOGGER.info("Retrieving all performers");
        ArrayList<PerformerDTO> dtoPerformers = new ArrayList<PerformerDTO>();

        try {
            em.getTransaction().begin();
            TypedQuery<Performer> query = em.createQuery("select p from Performer p", Performer.class);
            List<Performer> performers = query.getResultList();

            for (Performer performer : performers) {
                dtoPerformers.add(PerformerMapper.toDto(performer));
            }

            em.getTransaction().commit();
        } finally {
            em.close();
        }

        return Response
                .ok(dtoPerformers)
                .build();

    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(UserDTO creds, @CookieParam("auth") Cookie auth) {
        LOGGER.info("Attempting to login");
        NewCookie newCookie = new NewCookie("auth", UUID.randomUUID().toString());
        LOGGER.info("Generated cookie: " + newCookie.getValue());
        try {
            em.getTransaction().begin();

            User toFind = UserMapper.toDomainModel(creds);
            TypedQuery<User> query = em
                    .createQuery("select u from User u where u.username=:username and u.password=:password", User.class)
                    .setParameter("username", toFind.getUsername())
                    .setParameter("password", toFind.getPassword());

            if (query.getResultList().isEmpty()) {
                return Response.status(401).build();
            }

            User user = query.getSingleResult();
            user.setToken(newCookie.getValue());
            em.merge(user);
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        return Response
                .ok()
                .cookie(newCookie)
                .build();
    }

    @POST
    @Path("/bookings")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response makeBookingRequest(BookingRequestDTO bReq, @CookieParam("auth") Cookie auth) throws Exception {
        LOGGER.info("Attempt to create a booking request");

        ArrayList<Seat> seats = new ArrayList<Seat>();
        Booking booking;
        User user;
        Concert concert;

        if (auth == null) {
            return Response.status(401).build();
        }

        try {
            em.getTransaction().begin();
            for (String seatLabel : bReq.getSeatLabels()) {
                TypedQuery<Seat> seat = em
                        .createQuery("select s from Seat s where s.label=:label and s.date=:date", Seat.class)
                        .setParameter("label", seatLabel)
                        .setParameter("date", bReq.getDate());

                if (seat.getResultList().isEmpty()){
                    return Response.status(400).build();
                }

                if (seat.getSingleResult().getBookingStatus()){
                    return Response.status(403).build();
                }
                seat.getSingleResult().setBookingStatus(true);
                seats.add(seat.getSingleResult());
            }

            user = authenticate(auth);

            concert = em.find(Concert.class, bReq.getConcertId());
            if (concert == null){
                return Response.status(400).build();
            }

            booking = new Booking(bReq.getConcertId(), bReq.getDate(), seats);
            booking.setUser(user);
            em.persist(booking);
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        return Response
                .created(URI.create("/concert-service/bookings/" + booking.getId()))
                .build();
    }

    @GET
    @Path("/seats/{localDateTime}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSeats(@PathParam("localDateTime") LocalDateTimeParam dateParam, @QueryParam("status") String status, @CookieParam("auth") Cookie auth) {
        LOGGER.info("Attempting to get seats");

        LocalDateTime date = dateParam.getLocalDateTime();
        ArrayList<SeatDTO> seats = new ArrayList<SeatDTO>();

        try {
            em.getTransaction().begin();
            TypedQuery<Seat> seatQuery = em
                    .createQuery("select s from Seat s where s.date=:date and s.isBooked=:status", Seat.class)
                    .setParameter("date", date)
                    .setParameter("status", true);

            for (Seat seat : seatQuery.getResultList()) {
                seats.add(SeatMapper.toDto(seat));
            }

            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return Response
                .ok(seats)
                .build();
    }

    @GET
    @Path("/bookings/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBookingById(@CookieParam("auth") Cookie auth, @PathParam("id") long bookingId) {
        LOGGER.info("Attempting to get booking by id");
        BookingDTO dtoBooking;
        User user;

        if (auth == null) {
            return Response.status(403).build();
        }

        try {
            em.getTransaction().begin();
            user = authenticate(auth);
            Booking booking = em.find(Booking.class, bookingId);
            dtoBooking = BookingMapper.toDto(booking);

            if (booking.getUser().equals(user)) {
                return Response
                        .ok(dtoBooking)
                        .build();
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            return Response.status(401).build();
        } finally {
            em.close();
        }
        return Response.status(403).build();
    }

    public User authenticate(Cookie cookie) throws Exception {
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

    @GET
    @Path("/bookings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserBookings(@CookieParam("auth") Cookie auth) {
        LOGGER.info("Attempting to get user bookings");
        ArrayList<BookingDTO> bookingDTOS = new ArrayList<BookingDTO>();
        try {
            em.getTransaction().begin();
            User user = authenticate(auth);

            List<Booking> bookings = user.getBookings();
            for (Booking booking: bookings){
                bookingDTOS.add(BookingMapper.toDto(booking));
            }

            em.getTransaction().commit();
        } catch (Exception e) {
            return Response.status(401).build();
        } finally {
            em.close();
        }
        return Response
                .ok(bookingDTOS)
                .build();
    }

    private NewCookie makeCookie(String username, @CookieParam("auth") Cookie auth) {
        NewCookie newCookie = null;

        if (auth == null) {
            newCookie = new NewCookie(username, UUID.randomUUID().toString());
            LOGGER.info("Generated cookie: " + newCookie.getValue());
        }

        return newCookie;
    }

    private Cookie passCookie(@CookieParam("auth") Cookie auth) {
        return auth;
    }
}
