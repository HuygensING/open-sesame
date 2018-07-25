package nl.knaw.huc.di.sesame.auth.basic;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import nl.knaw.huc.di.sesame.auth.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class BasicAuthenticator implements Authenticator<BasicCredentials, User> {
  private static final Logger LOG = LoggerFactory.getLogger(BasicAuthenticator.class);

  @Override
  public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
    LOG.warn("credentials: {}", credentials);

    if ("OpenSesame".equals(credentials.getPassword())) {
      final User user = User.Builder.fromName(credentials.getUsername())
                                    // .withEmail("joe@example.com")
                                    // .identifiedBy("123-456-789")
                                    .build();

      return Optional.of(user);
    }

    return Optional.empty();
  }

}
