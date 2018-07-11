package nl.knaw.huc.di.auth;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class ExampleAuthenticator implements Authenticator<BasicCredentials, User> {
  private static final Logger LOG = LoggerFactory.getLogger(ExampleAuthenticator.class);

  @Override
  public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
    LOG.warn("credentials: {}", credentials);

    if ("OpenSesame".equals(credentials.getPassword())) {
      return Optional.of(new User(credentials.getUsername()));
    }

    return Optional.empty();
  }

}
