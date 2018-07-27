package nl.knaw.huc.di.sesame.auth.huygens;

import io.dropwizard.auth.AuthFilter;
import nl.knaw.huc.di.sesame.auth.SessionCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import java.net.URI;
import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

public class HuygensAuthFilter<P extends Principal> extends AuthFilter<SessionCredentials, P> {
  private static final Logger LOG = LoggerFactory.getLogger(HuygensAuthFilter.class);

  private HuygensAuthFilter() {
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    final String authHeader = requestContext.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

    final UUID uuid = getSessionId(authHeader);
    if (uuid == null) {
      deny();
    }

    final String host = Optional.ofNullable(requestContext.getUriInfo())
                                .map(UriInfo::getRequestUri)
                                .map(URI::getHost)
                                .orElse("unknown");

    final SessionCredentials credentials = new SessionCredentials(uuid, host);
    if (!authenticate(requestContext, credentials, "Huygens")) {
      deny();
    }
  }

  private UUID getSessionId(String header) {
    LOG.trace("header: [{}]", header);

    if (header == null) {
      LOG.debug("Missing auth header ");
      return null;
    }

    final int space = header.indexOf(' ');
    if (space <= 0) {
      LOG.debug("Short auth header");
      return null;
    }

    final String method = header.substring(0, space);
    if (!prefix.equalsIgnoreCase(method)) {
      LOG.trace("prefix {} != method {}", prefix, method);
      return null;
    }

    // Header prefix checked, from here on we're authoritative

    final String sessionId = header.substring(space + 1);
    LOG.trace("sessionId: [{}]", sessionId);

    try {
      return UUID.fromString(sessionId);
    } catch (IllegalArgumentException e) {
      LOG.warn("Session token not a valid uuid: {}", sessionId);
      return null;
    }
  }

  private void deny() {
    throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
  }

  public static class Builder<P extends Principal>
    extends AuthFilterBuilder<SessionCredentials, P, HuygensAuthFilter<P>> {
    @Override
    protected HuygensAuthFilter<P> newInstance() {
      return new HuygensAuthFilter<>();
    }
  }
}
