package proj.concert.service.util;

import proj.concert.service.domain.User;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.core.Cookie;

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
}
