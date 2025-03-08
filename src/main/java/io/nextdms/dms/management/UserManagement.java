package io.nextdms.dms.management;

import static io.nextdms.dms.SessionUtils.ungetSession;

import java.util.Map;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.oak.spi.security.privilege.PrivilegeConstants;
import org.apache.jackrabbit.oak.spi.security.user.UserConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//https://github.com/apache/sling-org-apache-sling-jcr-jackrabbit-usermanager/blob/master/src/main/java/org/apache/sling/jackrabbit/usermanager/impl/post/CreateUserServlet.java
public class UserManagement {

    private static final Logger LOG = LoggerFactory.getLogger(UserManagement.class);

    public User createUser(Session jcrSession, String username, String password, String passwordConfirm, Map<String, ?> properties)
        throws RepositoryException {
        if (jcrSession == null) {
            throw new RepositoryException("JCR Session not found");
        }
        // check that the submitted parameter values have valid values.
        if (username == null || username.isEmpty()) {
            throw new RepositoryException("User name was not submitted");
        }
        // check for an administrator
        boolean administrator = false;
        UserManager um = null;
        try {
            um = ((JackrabbitSession) jcrSession).getUserManager();
            User currentUser = (User) um.getAuthorizable(jcrSession.getUserID());
            administrator = currentUser.isAdmin();
            if (!administrator) {
                //check if the current user has the minimum privileges needed to create a user
                AccessControlManager acm = jcrSession.getAccessControlManager();
                administrator = acm.hasPrivileges(
                    UserConstants.PARAM_USER_PATH,
                    new Privilege[] {
                        acm.privilegeFromName(Privilege.JCR_READ),
                        acm.privilegeFromName(Privilege.JCR_READ_ACCESS_CONTROL),
                        acm.privilegeFromName(Privilege.JCR_MODIFY_ACCESS_CONTROL),
                        acm.privilegeFromName(PrivilegeConstants.REP_WRITE),
                        acm.privilegeFromName(PrivilegeConstants.REP_USER_MANAGEMENT),
                    }
                );
            }
        } catch (Exception ex) {
            LOG.warn("Failed to determine if the user is an admin, assuming not. Cause: {}", ex.getMessage());
            administrator = false;
        }

        if (!administrator) {
            throw new RepositoryException("Sorry, registration of new users is not currently enabled.  Please try again later.");
        }

        if (password == null) {
            throw new RepositoryException("Password was not submitted");
        }
        if (!password.equals(passwordConfirm)) {
            throw new RepositoryException("Password value does not match the confirmation password");
        }

        User user = null;
        try {
            Authorizable authorizable = um.getAuthorizable(username);

            if (authorizable != null) {
                // user already exists!
                throw new RepositoryException("A principal already exists with the requested name: " + username);
            } else {
                user = um.createUser(username, password);
                //TODO set properties
            }
        } finally {
            ungetSession(jcrSession);
        }

        return user;
    }
}
