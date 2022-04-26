package proj.concert.service.services;

import proj.concert.common.dto.*;
import proj.concert.service.common.Config;
import proj.concert.service.domain.*;
import proj.concert.service.jaxrs.LocalDateTimeParam;
import proj.concert.service.mapper.*;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
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
    public ConcertDTO retrieveConcert(@PathParam("id") long id) {
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

        return dtoConcert;

    }

    @GET
    @Path("/concerts")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<ConcertDTO> retrieveAllConcerts() {
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

        return dtoConcerts;

    }

    @GET
    @Path("/concerts/summaries")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<ConcertSummaryDTO> retrieveAllConcertSummaries() {
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

        return dtoConcertSummaries;
    }

    @GET
    @Path("/performers/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public PerformerDTO retrievePerformer(@PathParam("id") long id) {
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

        return dtoPerformer;
    }

    @GET
    @Path("/performers")
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayList<PerformerDTO> retrieveAllPerformers() {
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

        return dtoPerformers;

    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(UserDTO creds, @CookieParam("auth") Cookie auth) {
        LOGGER.info("Attempting to login");
        try {
            em.getTransaction().begin();

            User toFind = UserMapper.toDomainModel(creds);
            Query query = em
                    .createQuery("select u from User u where u.username=:username and u.password=:password", User.class)
                    .setParameter("username", toFind.getUsername())
                    .setParameter("password", toFind.getPassword());

            if (query.getResultList().isEmpty()) {
                return Response.status(401).build();
            }

            em.getTransaction().commit();
        } finally {
            em.close();
        }

        return Response
                .ok()
                .cookie(makeCookie(auth))
                .build();
    }

    @POST
    @Path("/bookings")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response makeBookingRequest(BookingRequestDTO bReq, @CookieParam("auth") Cookie auth) {
        LOGGER.info("Attempt to create a booking request");
        ArrayList<Seat> seats = new ArrayList<Seat>();
        Booking booking;

        if (auth == null) {
            return Response.status(401).build();
        }

        try {
            em.getTransaction().begin();
            for (String seatLabel: bReq.getSeatLabels()){
                TypedQuery<Seat> seat = em
                        .createQuery("select s from Seat s where s.label=:label and s.date=:date", Seat.class)
                        .setParameter("label", seatLabel)
                        .setParameter("date", bReq.getDate());
                seat.getSingleResult().setBookingStatus(true);
                seats.add(seat.getSingleResult());
            }

            booking = new Booking(bReq.getConcertId(), bReq.getDate(), seats);
            em.persist(booking);
            em.getTransaction().commit();
        } finally {
            em.close();
        }

        return Response
                //TODO sort out a good URI
                .created(URI.create("/concert-service/bookings/" + booking.getId()))
                .cookie(makeCookie(auth))
                .build();
    }

    @GET
    @Path("/seats/{localDateTime}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<SeatDTO> getSeats(@PathParam("localDateTime") LocalDateTimeParam dateParam, @QueryParam("status") String status, @CookieParam("auth") Cookie auth) {
        LOGGER.info("Attempting to get seats");

        LocalDateTime date = dateParam.getLocalDateTime();
        ArrayList<SeatDTO> seats = new ArrayList<SeatDTO>();

        try {
            em.getTransaction().begin();
            TypedQuery<Seat> seatQuery = em
                    .createQuery("select s from Seat s where s.date=:date and s.isBooked=:status", Seat.class)
                    .setParameter("date", date)
                    .setParameter("status", true);

            for (Seat seat: seatQuery.getResultList()){
                seats.add(SeatMapper.toDto(seat));
            }

            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return seats;
    }

    @GET
    @Path("/bookings/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public BookingDTO getBookingById(@CookieParam("auth") Cookie auth, @PathParam("id") long bookingId) {
        LOGGER.info("Attempting to get booking by id");
        BookingDTO dtoBooking;

        try {
            em.getTransaction().begin();
            Booking booking = em.find(Booking.class, bookingId);
            dtoBooking = BookingMapper.toDto(booking);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return dtoBooking;
    }


    //    @POST
//    @Consumes(MediaType.APPLICATION_JSON)
//    public Response createConcert(ConcertDTO dtoConcert) {
//        Concert concert = ConcertMapper.toDomainModel(dtoConcert);
//        try {
//            em.getTransaction().begin();
//            em.persist(concert);
//            em.getTransaction().commit();
//        } finally {
//            em.close();
//        }
//
//        return Response.created(URI.create("/concerts/" + concert.getId()))
//                .build();
//    }
//
//    @PUT
//    public Response updateConcert(Concert concert) {
//        try {
//            em.getTransaction().begin();
//            em.merge(concert);
//            em.getTransaction().commit();
//        } catch (IllegalArgumentException e) {
//            throw new WebApplicationException(Response.Status.NOT_FOUND);
//        } finally {
//            em.close();
//        }
//
//        return Response.noContent().build();
//    }
//
//    @DELETE
//    @Path("/{id}")
//    public Response deleteSingleConcert(@PathParam("id") long id) {
//        Concert concert;
//        try {
//            em.getTransaction().begin();
//            concert = em.find(Concert.class, id);
//            if(concert == null) {
//                throw new WebApplicationException(Response.Status.NOT_FOUND);
//            }
//
//            em.remove(concert);
//            em.getTransaction().commit();
//        } finally {
//            em.close();
//        }
//
//        return Response.noContent().build();
//    }
//
//
//    @DELETE
//    public Response deleteAllConcerts() {
//        try {
//            em.getTransaction().begin();
////            em.createQuery("DELETE FROM Concert");
//            em.getTransaction().commit();
//        } finally {
//            em.close();
//        }
//
//        return Response.noContent().build();
//    }
    private NewCookie makeCookie(@CookieParam("auth") Cookie auth) {
        NewCookie newCookie = null;

        if (auth == null) {
            newCookie = new NewCookie(Config.CLIENT_COOKIE, UUID.randomUUID().toString());
            LOGGER.info("Generated cookie: " + newCookie.getValue());
        }

        return newCookie;
    }
}
