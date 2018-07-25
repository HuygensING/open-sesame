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

    return "Aladdin".equals(user.getName()) && "ADMIN".equals(role);
  }
}
