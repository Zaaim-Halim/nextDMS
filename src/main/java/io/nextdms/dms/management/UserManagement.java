package io.nextdms.dms.management;

import static io.nextdms.dms.SessionUtils.ungetSession;

import io.nextdms.dms.SessionUtils;
import io.nextdms.dms.config.OakProperties;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
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
import org.springframework.stereotype.Component;

//https://github.com/apache/sling-org-apache-sling-jcr-jackrabbit-usermanager/blob/master/src/main/java/org/apache/sling/jackrabbit/usermanager/impl/post/CreateUserServlet.java
@Component
public class UserManagement {

    private static final Logger LOG = LoggerFactory.getLogger(UserManagement.class);

    /**
     * Create a user in the Oak repository.
     *
     * @param jcrSession the JCR session to use
     * @param username the username to create
     * @param password the password for the new user
     * @param passwordConfirm confirmation of the password
     * @param properties additional properties to set on the user
     * @return the created User object
     * @throws RepositoryException if an error occurs
     */
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

    /**
     * Assign or reassign authorities to a user in the Oak repository using an existing session.
     *
     * @param jcrSession the JCR session to use
     * @param username the username of the user to update
     * @param authorities the set of authority names to assign to the user
     * @return true if successful, false otherwise
     */
    public boolean assignAuthorities(Session jcrSession, String username, Set<String> authorities) {
        LOG.debug("Assigning authorities {} to user {} using provided session", authorities, username);

        try {
            UserManager userManager = ((JackrabbitSession) jcrSession).getUserManager();
            Authorizable authorizable = userManager.getAuthorizable(username);

            if (authorizable == null || authorizable.isGroup()) {
                LOG.error("User {} not found or is a group", username);
                return false;
            }

            User user = (User) authorizable;

            // Create Value objects for the authorities
            ValueFactory valueFactory = jcrSession.getValueFactory();
            Value[] authorityValues = authorities
                .stream()
                .map(authority -> {
                    try {
                        return valueFactory.createValue(authority);
                    } catch (Exception e) {
                        LOG.error("Error creating value for authority {}", authority, e);
                        return null;
                    }
                })
                .filter(value -> value != null)
                .toArray(Value[]::new);

            // Check if the user already has authorities property
            if (user.hasProperty("authorities")) {
                // Get existing authorities for logging
                Value[] existingAuthorities = user.getProperty("authorities");
                LOG.debug(
                    "Replacing existing authorities {} with new authorities {} for user {}",
                    Arrays.toString(existingAuthorities),
                    authorities,
                    username
                );
            }

            // Set the new authorities
            user.setProperty("authorities", authorityValues);

            // Save changes
            jcrSession.save();

            LOG.info("Successfully assigned authorities {} to user {}", authorities, username);
            return true;
        } catch (RepositoryException e) {
            LOG.error("Error assigning authorities to user {} with provided session", username, e);
            return false;
        }
    }

    /**
     * Add authorities to a user's existing authorities in the Oak repository using an existing session.
     *
     * @param jcrSession the JCR session to use
     * @param username the username of the user to update
     * @param authorities the set of authority names to add to the user
     * @return true if successful, false otherwise
     */
    public boolean addAuthorities(Session jcrSession, String username, Set<String> authorities) {
        LOG.debug("Adding authorities {} to user {} using provided session", authorities, username);

        try {
            UserManager userManager = ((JackrabbitSession) jcrSession).getUserManager();
            Authorizable authorizable = userManager.getAuthorizable(username);

            if (authorizable == null || authorizable.isGroup()) {
                LOG.error("User {} not found or is a group", username);
                return false;
            }

            User user = (User) authorizable;
            ValueFactory valueFactory = jcrSession.getValueFactory();

            // Combine existing and new authorities
            Set<String> combinedAuthorities = authorities;

            if (user.hasProperty("authorities")) {
                Value[] existingAuthorityValues = user.getProperty("authorities");
                Set<String> existingAuthorities = Arrays.stream(existingAuthorityValues)
                    .map(value -> {
                        try {
                            return value.getString();
                        } catch (RepositoryException e) {
                            LOG.error("Error reading authority value", e);
                            return null;
                        }
                    })
                    .filter(auth -> auth != null)
                    .collect(Collectors.toSet());

                // Add existing authorities to the set (duplicates will be ignored)
                combinedAuthorities.addAll(existingAuthorities);

                LOG.debug("Adding new authorities {} to existing authorities {} for user {}", authorities, existingAuthorities, username);
            }

            // Create Value objects for the combined authorities
            Value[] authorityValues = combinedAuthorities
                .stream()
                .map(authority -> {
                    try {
                        return valueFactory.createValue(authority);
                    } catch (Exception e) {
                        LOG.error("Error creating value for authority {}", authority, e);
                        return null;
                    }
                })
                .filter(value -> value != null)
                .toArray(Value[]::new);

            // Set the combined authorities
            user.setProperty("authorities", authorityValues);

            // Save changes
            jcrSession.save();

            LOG.info("Successfully added authorities to user {}, final authorities: {}", username, combinedAuthorities);
            return true;
        } catch (RepositoryException e) {
            LOG.error("Error adding authorities to user {} with provided session", username, e);
            return false;
        }
    }

    /**
     * Remove specified authorities from a user in the Oak repository using an existing session.
     *
     * @param jcrSession the JCR session to use
     * @param username the username of the user to update
     * @param authorities the set of authority names to remove from the user
     * @return true if successful, false otherwise
     */
    public boolean removeAuthorities(Session jcrSession, String username, Set<String> authorities) {
        LOG.debug("Removing authorities {} from user {} using provided session", authorities, username);

        try {
            UserManager userManager = ((JackrabbitSession) jcrSession).getUserManager();
            Authorizable authorizable = userManager.getAuthorizable(username);

            if (authorizable == null || authorizable.isGroup()) {
                LOG.error("User {} not found or is a group", username);
                return false;
            }

            User user = (User) authorizable;

            if (!user.hasProperty("authorities")) {
                LOG.debug("User {} has no authorities to remove", username);
                return true; // Nothing to remove
            }

            // Get existing authorities
            Value[] existingAuthorityValues = user.getProperty("authorities");
            Set<String> existingAuthorities = Arrays.stream(existingAuthorityValues)
                .map(value -> {
                    try {
                        return value.getString();
                    } catch (RepositoryException e) {
                        LOG.error("Error reading authority value", e);
                        return null;
                    }
                })
                .filter(auth -> auth != null)
                .collect(Collectors.toSet());

            // Remove the specified authorities
            existingAuthorities.removeAll(authorities);

            LOG.debug("Removing authorities {} from existing authorities, resulting in {}", authorities, existingAuthorities);

            if (existingAuthorities.isEmpty()) {
                // If no authorities left, remove the property
                user.removeProperty("authorities");
                LOG.debug("All authorities removed from user {}", username);
            } else {
                // Create Value objects for the remaining authorities
                ValueFactory valueFactory = jcrSession.getValueFactory();
                Value[] newAuthorityValues = existingAuthorities
                    .stream()
                    .map(authority -> {
                        try {
                            return valueFactory.createValue(authority);
                        } catch (Exception e) {
                            LOG.error("Error creating value for authority {}", authority, e);
                            return null;
                        }
                    })
                    .filter(value -> value != null)
                    .toArray(Value[]::new);

                // Set the remaining authorities
                user.setProperty("authorities", newAuthorityValues);
            }

            // Save changes
            jcrSession.save();

            LOG.info("Successfully removed authorities from user {}, remaining authorities: {}", username, existingAuthorities);
            return true;
        } catch (RepositoryException e) {
            LOG.error("Error removing authorities from user {} with provided session", username, e);
            return false;
        }
    }

    /**
     * Get the current authorities assigned to a user using an existing session.
     *
     * @param jcrSession the JCR session to use
     * @param username the username of the user
     * @return a set of authority names, or empty set if user not found or has no authorities
     */
    public Set<String> getUserAuthorities(Session jcrSession, String username) {
        LOG.debug("Getting authorities for user {} using provided session", username);

        try {
            UserManager userManager = ((JackrabbitSession) jcrSession).getUserManager();
            Authorizable authorizable = userManager.getAuthorizable(username);

            if (authorizable == null || authorizable.isGroup()) {
                LOG.error("User {} not found or is a group", username);
                return Set.of();
            }

            User user = (User) authorizable;

            if (!user.hasProperty("authorities")) {
                LOG.debug("User {} has no authorities", username);
                return Set.of();
            }

            // Get authorities
            Value[] authorityValues = user.getProperty("authorities");
            Set<String> authorities = Arrays.stream(authorityValues)
                .map(value -> {
                    try {
                        return value.getString();
                    } catch (RepositoryException e) {
                        LOG.error("Error reading authority value", e);
                        return null;
                    }
                })
                .filter(auth -> auth != null)
                .collect(Collectors.toSet());

            LOG.debug("Found authorities for user {}: {}", username, authorities);
            return authorities;
        } catch (RepositoryException e) {
            LOG.error("Error getting authorities for user {} with provided session", username, e);
            return Set.of();
        }
    }

    /**
     * Assign or reassign a single authority to a user in the Oak repository using an existing session.
     *
     * @param jcrSession the JCR session to use
     * @param username the username of the user to update
     * @param authority the authority name to assign to the user
     * @return true if successful, false otherwise
     */
    public boolean assignAuthority(Session jcrSession, String username, String authority) {
        return assignAuthorities(jcrSession, username, Set.of(authority));
    }

    /**
     * Assign or reassign authorities to a user in the Oak repository using an existing session and an array of authority names.
     *
     * @param jcrSession the JCR session to use
     * @param username the username of the user to update
     * @param authorities the array of authority names to assign to the user
     * @return true if successful, false otherwise
     */
    public boolean assignAuthorities(Session jcrSession, String username, String... authorities) {
        return assignAuthorities(jcrSession, username, Set.of(authorities));
    }
}
