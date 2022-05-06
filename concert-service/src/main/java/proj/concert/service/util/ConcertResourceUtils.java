package proj.concert.service.util;

import proj.concert.service.domain.Concert;
import proj.concert.service.domain.Performer;
import proj.concert.service.domain.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;

import static org.hibernate.tool.schema.SchemaToolingLogging.LOGGER;

public class ConcertResourceUtils {

    // TODO Look at creating custom exception for this
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

    public static Concert findConcert(EntityManager em, Long id, String method){
        Concert concert = em.find(Concert.class, id);
        if (concert == null){
            exceptionDecisionManager(method);
        }
        return concert;
    }

    public static Performer findPerformer(EntityManager em, Long id, String method){
        Performer performer = em.find(Performer.class, id);
        if (performer == null){
            exceptionDecisionManager(method);
        }
        return performer;
    }

    public static void exceptionDecisionManager(String method){
        switch(method){
            case "GET":
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            case "POST":
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }
}
