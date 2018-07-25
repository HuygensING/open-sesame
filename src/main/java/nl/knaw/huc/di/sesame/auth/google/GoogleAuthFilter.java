package nl.knaw.huc.di.sesame.auth.google;

import io.dropwizard.auth.AuthFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;

import java.security.Principal;
import java.util.UUID;

public class GoogleAuthFilter<P extends Principal> extends AuthFilter<UUID, P> {
  private static final Logger LOG = LoggerFactory.getLogger(GoogleAuthFilter.class);

  private GoogleAuthFilter() {
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    final String authHeader = requestContext.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

    final UUID sessionId = getSessionId(authHeader);
    if (!authenticate(requestContext, sessionId, "Google")) {
      throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
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
      LOG.debug("prefix {} != method {}", prefix, method);
      return null; // not for us, maybe some other AuthFilter can pick it up
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

  public static class Builder<P extends Principal> extends AuthFilterBuilder<UUID, P, GoogleAuthFilter<P>> {
    @Override
    protected GoogleAuthFilter<P> newInstance() {
      return new GoogleAuthFilter<>();
    }
  }
}
