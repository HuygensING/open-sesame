package nl.knaw.huc.di.sesame.auth.basic;

import io.dropwizard.auth.Authorizer;
import nl.knaw.huc.di.sesame.auth.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicAuthorizer implements Authorizer<User> {
  private static final Logger LOG = LoggerFactory.getLogger(BasicAuthorizer.class);

  @Override
  public boolean authorize(User user, String role) {
    LOG.warn("User: {}, role: {}", user, role);

    final String name = user.getName();

    switch (role) {
      case "ADMIN":
        return "Hayco de Jong".equals(name);

      case "LOCAL":
        final boolean isAccessingLocalhost = user.getHost().map("localhost"::equals).orElse(false);
        LOG.trace("Checking if {} is accessing a localhost URI: {}", user.getName(), isAccessingLocalhost);
        return isAccessingLocalhost;

      default:
        return "Aladdin".equals(name);
    }
  }
}
