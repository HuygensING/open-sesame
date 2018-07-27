package nl.knaw.huc.di.sesame.auth;

import io.dropwizard.auth.Authorizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DefaultAuthorizer implements Authorizer<User> {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultAuthorizer.class);

  private final List<String> localAddresses;

  DefaultAuthorizer(List<String> localAddresses) {
    this.localAddresses = localAddresses;
  }

  @Override
  public boolean authorize(User user, String role) {
    LOG.warn("User: {}, role: {}", user, role);

    final String name = user.getName();

    switch (role) {
      case "ADMIN":
        return "Hayco de Jong".equals(name);

      case "LOCAL":
        return isLocal(user);

      default:
        return "Aladdin".equals(name);
    }
  }

  private boolean isLocal(User user) {
    return user.getRemoteAddr().map(localAddresses::contains).orElse(false);
  }

}
