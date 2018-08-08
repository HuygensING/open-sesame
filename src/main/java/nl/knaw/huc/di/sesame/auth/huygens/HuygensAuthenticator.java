package nl.knaw.huc.di.sesame.auth.huygens;

import io.dropwizard.auth.Authenticator;
import nl.knaw.huc.di.sesame.SesameConfiguration;
import nl.knaw.huc.di.sesame.auth.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class HuygensAuthenticator implements Authenticator<UUID, User> {
  private static final Logger LOG = LoggerFactory.getLogger(HuygensAuthenticator.class);

  private final Client client;
  private final String securityServerURL;
  private final String credentials;

  @Context
  private HttpServletRequest servletRequest;

  public HuygensAuthenticator(Client client, SesameConfiguration.FederatedAuthConfig config) {
    this.client = checkNotNull(client);
    this.securityServerURL = config.getUrl();
    this.credentials = config.getCredentials();
  }

  @Override
  public Optional<User> authenticate(UUID sessionId) {
    LOG.debug("Authenticating session: {}", sessionId);

    LOG.trace("targeting security server: {}", securityServerURL);
    final HuygensDetails huygensDetails;

    try {
      huygensDetails = client.target(securityServerURL)
                             .path("sessions").path(sessionId.toString())
                             .request()
                             .accept(MediaType.APPLICATION_JSON_TYPE)
                             .header(HttpHeaders.AUTHORIZATION, credentials)
                             .get(HuygensDetails.class);
    } catch (WebApplicationException e) {
      if (e.getResponse() != null) {
        final String message = e.getResponse().readEntity(String.class);
        if (message.startsWith("Unknown session:")) {
          LOG.trace(message);
        } else {
          LOG.warn("Exception while dealing with security server: {}", message);
        }
      }
      return Optional.empty(); // hide issue
    }

    LOG.trace("huygensDetails: {}", huygensDetails);

    if (huygensDetails == null) {
      return Optional.empty();
    }

    final HuygensDetails.Owner owner = huygensDetails.getOwner();

    if (owner == null) {
      return Optional.empty();
    }

    return Optional.of(User.Builder.fromName(owner.getDisplayName())
                                   .identifiedBy(owner.getPersistentID())
                                   .withEmail(owner.getEmailAddress())
                                   .withRemoteAddr(servletRequest.getRemoteAddr())
                                   .build());
  }
}
