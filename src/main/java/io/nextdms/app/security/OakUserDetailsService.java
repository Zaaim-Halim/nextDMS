package io.nextdms.app.security;

import io.nextdms.dms.SessionUtils;
import io.nextdms.dms.config.OakProperties;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Authenticate a user stored in Oak instead from database.
 */
@ConditionalOnMissingBean(UserDetailsService.class)
@Component("okakUserDetailsService")
public class OakUserDetailsService implements UserDetailsService {

    private final Repository repository;
    private final OakProperties oakProperties;

    public OakUserDetailsService(Repository repository, OakProperties oakProperties) {
        this.repository = repository;
        this.oakProperties = oakProperties;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Session jcrSession = repository.login(
                new SimpleCredentials(oakProperties.getAdmin().getUsername(), oakProperties.getAdmin().getPassword().toCharArray())
            );
            UserManager userManager = ((JackrabbitSession) jcrSession).getUserManager();
            User user = (User) userManager.getAuthorizable(username);
            if (user != null) {
                final var password = user.getProperty("password")[0].getString();
                final var authorities = Arrays.stream(user.getProperty("authorities"))
                    .filter(Objects::nonNull)
                    .map(v -> {
                        try {
                            return new SimpleGrantedAuthority(v.getString());
                        } catch (RepositoryException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toSet());
                SessionUtils.ungetSession(jcrSession);
                return new org.springframework.security.core.userdetails.User(username, password, authorities);
            } else {
                throw new UsernameNotFoundException("User " + username + " not found");
            }
        } catch (RepositoryException e) {
            throw new UsernameNotFoundException("Error accessing repository", e);
        }
    }
}
