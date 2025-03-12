package io.nextdms.dms;

import io.nextdms.dms.config.OakProperties;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
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

    public static Session getSessionForExplorer(Repository repository, OakProperties oakProperties) {
        try {
            return repository.login(
                new SimpleCredentials(oakProperties.getAdmin().getUsername(), oakProperties.getAdmin().getPassword().toCharArray())
            );
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Failed to get session for explorer", e);
            }
            throw new RuntimeException("Failed to get session for explorer", e);
        }
    }
}
