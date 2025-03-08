package io.nextdms.dms;

import javax.jcr.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionUtils {

    private static final Logger LOG = LoggerFactory.getLogger(SessionUtils.class);

    public static void ungetSession(final Session session) {
        if (session != null) {
            try {
                session.logout();
            } catch (Exception t) {
                LOG.error(String.format("Unable to log out of session: %s", t.getMessage()), t);
            }
        }
    }
}
