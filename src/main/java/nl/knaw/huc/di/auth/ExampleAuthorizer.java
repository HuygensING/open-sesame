package nl.knaw.huc.di.auth;

import io.dropwizard.auth.Authorizer;

public class ExampleAuthorizer implements Authorizer<User> {
  @Override
  public boolean authorize(User user, String role) {
    return "Aladdin".equals(user.getName()) && "ADMIN".equals(role);
  }
}
