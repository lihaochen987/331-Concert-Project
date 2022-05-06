package proj.concert.service.services;

import proj.concert.common.dto.*;
import proj.concert.common.types.BookingStatus;
import proj.concert.service.domain.*;
import proj.concert.service.jaxrs.LocalDateTimeParam;
import proj.concert.service.mapper.*;
import proj.concert.service.util.ConcertResourceUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.*;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;

import static org.hibernate.tool.schema.SchemaToolingLogging.LOGGER;
import static proj.concert.service.util.ConcertResourceUtils.*;

/**
 * This is a class that implements endpoints for a concert application
 * <p>
 * - GET    <base-uri>/concert-service/concerts/{id}
 * Retrieves concert from a given id
 * <p>
 * - GET    <base-uri>/concert-service/concerts
 * Retrieves all concerts
 * <p>
 * - GET    <base-uri>/concert-service/concerts/summaries
 * Retrieves concert summaries
 * <p>
 * - GET    <base-uri>/concert-service/performers/{id}
 * Retrieves performer from given id
 * <p>
 * - GET    <base-uri>/concert-service/performers
 * Retrieves all performers
 * <p>
 * - POST   <base-uri>/login
 * Login to concert service
 * <p>
 * - POST   <base-uri>/bookings
 * Makes a booking request
 * <p>
 * - GET    <base-uri>/seats/{localdatetime}
 * Retrieve seats for a given date
 * <p>
 * - GET    <base-uri>/bookings/{id}
 * Retrieves booking with given id
 * <p>
 * - GET    <base-uri>/bookings
 * Retrieves a logged in users bookings
 */

@Path("/concert-service")
public class ConcertResource {

    private EntityManager em = PersistenceManager.instance().createEntityManager();
    private static final Map<AsyncResponse, ConcertInfoSubscriptionDTO> subs = new HashMap<>();

    /**
     * Attempts to retrieve a concert with supplied concertId. If a valid concert is found a concert object
     * will be passed to the client. If no valid concert is found a 404 error is returned to the client
     *
     * @param id   the unique id for a specific concert
     * @param auth the user auth token
     * @return a JSON object representation of a given concert
     */
    @GET
    @Path("/concerts/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveConcert(@PathParam("id") long id, @CookieParam("auth") Cookie auth) {
        LOGGER.info("Retrieving concert with id: " + id);
        ConcertDTO dtoConcert;
        Concert concert;
        try {
            em.getTransaction().begin();
            concert = findConcert(em, id, "GET");
            dtoConcert = ConcertMapper.toDto(concert);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return Response.ok(dtoConcert).build();
    }

    /**
     * Attempts to retrieve all concerts
     *
     * @param auth the user auth token
     * @return a JSON object representation of all concerts
     */
    @GET
    @Path("/concerts")
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveAllConcerts(@CookieParam("auth") Cookie auth) {
        LOGGER.info("Retrieving all concerts");
        ArrayList<ConcertDTO> dtoConcerts = new ArrayList<ConcertDTO>();
        List<Concert> concerts;
        try {
            em.getTransaction().begin();
            concerts = findAllConcerts(em);
            for (Concert concert : concerts) {
                dtoConcerts.add(ConcertMapper.toDto(concert));
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return Response.ok(dtoConcerts).build();
    }

    /**
     * Attempts to retrieve all concert summaries
     *
     * @param auth the user auth token
     * @return a JSON object representation of all concert summaries
     */
    @GET
    @Path("/concerts/summaries")
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrieveAllConcertSummaries(@CookieParam("auth") Cookie auth) {
        LOGGER.info("Retrieving all concert summaries");
        ArrayList<ConcertSummaryDTO> dtoConcertSummaries = new ArrayList<ConcertSummaryDTO>();
        List<Concert> concerts;
        try {
            em.getTransaction().begin();
            concerts = findAllConcerts(em);
            for (Concert concert : concerts) {
                ConcertSummaryDTO toAdd = new ConcertSummaryDTO(concert.getId(), concert.getTitle(), concert.getImageName());
                dtoConcertSummaries.add(toAdd);
            }
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return Response.ok(dtoConcertSummaries).build();
    }

    /**
     * Attempts to retrieve a performer with a given id. If a performer is found the performer object
     * will be passed to the client. If the performer is not found a 404 error is returned.
     *
     * @param id   the unique id of a specific concert
     * @param auth the user auth token
     * @return a JSON object representation of a given performer
     */
    @GET
    @Path("/performers/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response retrievePerformer(@PathParam("id") long id, @CookieParam("auth") Cookie auth) {
        LOGGER.info("Retrieving performer with id: " + id);
        PerformerDTO dtoPerformer;
        Performer performer;
        try {
            em.getTransaction().begin();
            performer = findPerformer(em, id, "GET");
            dtoPerformer = PerformerMapper.toDto(performer);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return Response.ok(dtoPerformer).build();
    }

    /**
     * Attempts to retrieve all performers
     *
     * @param auth the user auth token
     * @return a JSON representation of all performer objects
     */
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
        return Response.ok(dtoPerformers).build();
    }

    /**
     * Attempts to login a user. If a valid username and password is found in the database
     * the auth token will be updated to a new value and a 200 error code is returned. If
     * no user is found a 401 unauthorized error will be returned to the client.
     *
     * @param creds the supplied username and password
     * @param auth  the user auth token
     * @return a response
     */
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(UserDTO creds, @CookieParam("auth") Cookie auth) {
        LOGGER.info("Attempting to login");
        NewCookie newCookie = new NewCookie("auth", UUID.randomUUID().toString());
        LOGGER.info("Generated cookie: " + newCookie.getValue());
        try {
            em.getTransaction().begin();
            findUserAndAssignToken(em, creds, newCookie);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return Response.ok().cookie(newCookie).build();
    }

    /**
     * Attempts to create a booking request for a logged in user. If authentication fails a 401 unauthorized
     * error is returned to the client. If the booking request is invalid a 400 bad request is returned. If
     * all else succeeds a booking is made.
     *
     * @param bReq booking request object
     * @param auth the user auth token
     * @return
     * @throws Exception
     */
    @POST
    @Path("/bookings")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response makeBookingRequest(BookingRequestDTO bReq, @CookieParam("auth") Cookie auth) throws Exception {
        LOGGER.info("Attempt to create a booking request");

        ArrayList<Seat> seats = new ArrayList<Seat>();
        Booking booking;
        User user;
        Concert concert;
        checkAuthenticationNotNull(auth);
        try {
            em.getTransaction().begin();
            user = authenticate(em, auth);
            for (String seatLabel : bReq.getSeatLabels()) {
                TypedQuery<Seat> seat = em
                        .createQuery("select s from Seat s where s.label=:label and s.date=:date", Seat.class)
                        .setParameter("label", seatLabel)
                        .setParameter("date", bReq.getDate());

                if (seat.getResultList().isEmpty()) {
                    return Response.status(400).build();
                }

                if (seat.getSingleResult().getBookingStatus()) {
                    return Response.status(403).build();
                }
                seat.getSingleResult().setBookingStatus(true);
                seats.add(seat.getSingleResult());
            }
            concert = findConcert(em, bReq.getConcertId(), "POST");

            booking = new Booking(bReq.getConcertId(), bReq.getDate(), seats);
            booking.setUser(user);
            em.persist(booking);

            TypedQuery<Seat> seatQuery = em
                    .createQuery("select s from Seat s where s.date=:date and s.isBooked=:status", Seat.class)
                    .setParameter("date", booking.getDate())
                    .setParameter("status", false);

            // Check seat amount left and push to specific subscriptions
            int percentageFree = (int) ((((double) seatQuery.getResultList().size()) / 120.0) * 100);
            ConcertInfoNotificationDTO notif = new ConcertInfoNotificationDTO(seatQuery.getResultList().size());

            for (Map.Entry<AsyncResponse, ConcertInfoSubscriptionDTO> entry : subs.entrySet()) {
                // Check relavent concert information
                if (entry.getValue().getConcertId() == concert.getId() && concert.getDates().contains(entry.getValue().getDate())) {
                    if (percentageFree < entry.getValue().getPercentageBooked()) {
                        entry.getKey().resume(notif);
                    }
                }
            }

            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return Response.created(URI.create("/concert-service/bookings/" + booking.getId())).build();
    }

    @GET
    @Path("/seats/{localDateTime}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSeats(@PathParam("localDateTime") LocalDateTimeParam dateParam, @QueryParam("status") String status, @CookieParam("auth") Cookie auth) {
        LOGGER.info("Attempting to get seats");

        LocalDateTime date = dateParam.getLocalDateTime();
        ArrayList<SeatDTO> seats = new ArrayList<SeatDTO>();
        TypedQuery<Seat> seatQuery = null;

        try {
            BookingStatus bookingStatus = BookingStatus.valueOf(status);
            em.getTransaction().begin();

            switch (bookingStatus) {
                case Any:
                    seatQuery = em
                            .createQuery("select s from Seat s where s.date=:date", Seat.class)
                            .setParameter("date", date);
                    break;

                case Booked:
                    seatQuery = em
                            .createQuery("select s from Seat s where s.date=:date and s.isBooked=:status", Seat.class)
                            .setParameter("date", date)
                            .setParameter("status", true);
                    break;
                case Unbooked:
                    seatQuery = em
                            .createQuery("select s from Seat s where s.date=:date and s.isBooked=:status", Seat.class)
                            .setParameter("date", date)
                            .setParameter("status", false);
                    break;
            }

            for (Seat seat : seatQuery.getResultList()) {
                seats.add(SeatMapper.toDto(seat));
            }

            em.getTransaction().commit();
        } catch (IllegalArgumentException e) {
            // Catches any illegal arguments supplied in @QueryParam status
            return Response.status(Response.Status.BAD_REQUEST).build();

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
        checkAuthenticationNotNull(auth);
        try {
            em.getTransaction().begin();
            user = authenticate(em, auth);
            Booking booking = em.find(Booking.class, bookingId);
            dtoBooking = BookingMapper.toDto(booking);

            if (booking.getUser().equals(user)) {
                return Response.ok(dtoBooking).build();
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            return Response.status(401).build();
        } finally {
            em.close();
        }
        return Response.status(403).build();
    }


    @GET
    @Path("/bookings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserBookings(@CookieParam("auth") Cookie auth) {
        LOGGER.info("Attempting to get user bookings");
        ArrayList<BookingDTO> bookingDTOS = new ArrayList<BookingDTO>();
        try {
            em.getTransaction().begin();
            User user = authenticate(em, auth);

            List<Booking> bookings = user.getBookings();
            for (Booking booking : bookings) {
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

    @POST
    @Path("/subscribe/concertInfo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void subscribeToConcert(@Suspended AsyncResponse sub, @CookieParam("auth") Cookie auth, ConcertInfoSubscriptionDTO subscriptionDTO) throws InterruptedException {
        LOGGER.info("Attempting to subscribe user to concert");
        Concert concert;
        if (auth == null) {
            sub.resume(Response.status(Response.Status.UNAUTHORIZED).build());
        } else {

            try {
                em.getTransaction().begin();
                // Check ConcertInfoSubscriptionDTO values
                concert = findConcert(em, subscriptionDTO.getConcertId(), "POST");

                if (!concert.getDates().contains(subscriptionDTO.getDate())) {
                    sub.resume(Response.status(Response.Status.BAD_REQUEST).build());
                } else {
                    subs.put(sub, subscriptionDTO);
                }

                em.getTransaction().commit();

            } finally {
                em.close();
            }
        }

    }

    private NewCookie makeCookie(String username, @CookieParam("auth") Cookie auth) {
        NewCookie newCookie = null;

        if (auth == null) {
            newCookie = new NewCookie(username, UUID.randomUUID().toString());
            LOGGER.info("Generated cookie: " + newCookie.getValue());
        }

        return newCookie;
    }

}
