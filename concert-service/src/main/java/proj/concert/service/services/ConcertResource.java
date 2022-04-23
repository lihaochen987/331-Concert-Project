package proj.concert.service.services;

import proj.concert.common.dto.ConcertDTO;
import proj.concert.common.dto.ConcertSummaryDTO;
import proj.concert.service.domain.Concert;
import proj.concert.service.domain.ConcertSummary;
import proj.concert.service.jaxrs.LocalDateTimeParam;
import proj.concert.service.mapper.ConcertMapper;
import proj.concert.service.mapper.ConcertSummaryMapper;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

}
