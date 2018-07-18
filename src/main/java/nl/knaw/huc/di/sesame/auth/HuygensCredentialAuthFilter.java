package nl.knaw.huc.di.sesame.auth;

import io.dropwizard.auth.AuthFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.HttpHeaders;

import java.io.IOException;
import java.security.Principal;
import java.util.UUID;

public class HuygensCredentialAuthFilter<P extends Principal> extends AuthFilter<UUID, P> {
  private static final Logger LOG = LoggerFactory.getLogger(HuygensCredentialAuthFilter.class);

  private HuygensCredentialAuthFilter() {
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    final String authHeader = requestContext.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

    final UUID sessionId = getSessionId(authHeader);
    if (!authenticate(requestContext, sessionId, "HUYGENS")) {
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
      LOG.debug("Short auth header") ;
      return null;
    }

    final String method = header.substring(0, space);
    if (!prefix.equalsIgnoreCase(method)) {
      LOG.debug("prefix {} != method {}", prefix, method);
      return null;
    }

    final String sessionId = header.substring(space + 1);
    LOG.trace("sessionId: [{}]", sessionId);

    try {
      return UUID.fromString(sessionId);
    } catch (IllegalArgumentException e) {
      LOG.warn("Session token not a valid uuid: {}", sessionId);
      return null;
    }
  }

  public static class Builder<P extends Principal> extends
    AuthFilterBuilder<UUID, P, HuygensCredentialAuthFilter<P>> {

    @Override
    protected HuygensCredentialAuthFilter<P> newInstance() {
      return new HuygensCredentialAuthFilter<>();
    }
  }
}
