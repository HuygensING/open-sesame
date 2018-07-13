package nl.knaw.huc.di.sesame.auth;

import io.dropwizard.auth.Authenticator;
import nl.knaw.huc.di.sesame.SesameConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
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

  public HuygensAuthenticator(Client client, SesameConfiguration.FederatedAuthConfig config) {
    this.client = checkNotNull(client);
    this.securityServerURL = config.getUrl();
    this.credentials = config.getCredentials();
  }

  @Override
  public Optional<User> authenticate(UUID sessionID) {
    LOG.debug("Authenticating session: {}", sessionID);
    LOG.trace("targeting URL: {}", securityServerURL);

    final Session session = client.target(securityServerURL).path("sessions").path(sessionID.toString())
                                  .request()
                                  .accept(MediaType.APPLICATION_JSON_TYPE)
                                  .header(HttpHeaders.AUTHORIZATION, credentials)
                                  .get(Session.class);

    LOG.debug("session: {}", session);
    if (session != null && session.getOwner() != null) {
      return Optional.of(new User(session.getOwner().getDisplayName()));
    }

    return Optional.empty();
  }
}
