package proj.concert.service.util;

import proj.concert.common.dto.UserDTO;
import proj.concert.service.domain.Concert;
import proj.concert.service.domain.Performer;
import proj.concert.service.domain.User;
import proj.concert.service.mapper.UserMapper;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import java.util.List;

import static org.hibernate.tool.schema.SchemaToolingLogging.LOGGER;

public class ConcertResourceUtils {

    // User helper functions
    public static void findUserAndAssignToken(EntityManager em, UserDTO dtoUser, NewCookie cookie) {
        User domainUser = UserMapper.toDomainModel(dtoUser);
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
    public static Performer findPerformer(EntityManager em, Long id, String method) {
        Performer performer = em.find(Performer.class, id);
        if (performer == null) {
            entityExceptionDecisionManager(method);
        }
        return performer;
    }

    // Seat helper functions


    // Utility functions
    public static void entityExceptionDecisionManager(String method) {
        switch (method) {
            case "GET":
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            case "POST":
                throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }
}
