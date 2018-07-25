package nl.knaw.huc.di.sesame.auth.huygens;

import io.dropwizard.auth.Authenticator;
import nl.knaw.huc.di.sesame.SesameConfiguration;
import nl.knaw.huc.di.sesame.auth.User;
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


    LOG.trace("targeting security server: {}", securityServerURL);
    final HuygensDetails huygensDetails = client.target(securityServerURL).path("sessions").path(sessionID.toString())
                                                .request()
                                                .accept(MediaType.APPLICATION_JSON_TYPE)
                                                .header(HttpHeaders.AUTHORIZATION, credentials)
                                                .get(HuygensDetails.class);
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
                                   .build());
  }
}
