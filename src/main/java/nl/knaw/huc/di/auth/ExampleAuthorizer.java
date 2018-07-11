package nl.knaw.huc.di.auth;

import io.dropwizard.auth.Authorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleAuthorizer implements Authorizer<User> {
  private static final Logger LOG = LoggerFactory.getLogger(ExampleAuthorizer.class);

  @Override
  public boolean authorize(User user, String role) {
    LOG.warn("User: {}, role: {}", user, role);

    return "Aladdin".equals(user.getName()) && "ADMIN".equals(role);
  }
}
