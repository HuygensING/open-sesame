package nl.knaw.huc.di.sesame.resources;

import io.dropwizard.auth.Auth;
import nl.knaw.huc.di.sesame.auth.User;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import java.util.Optional;

@Path("protected")
public class Protected {

  @GET
  public String getGreeting(@Auth Optional<User> userOpt) {
    final String name = userOpt.map(User::getName).orElse("anonymous user");
    return String.format("Hello, %s\n", name);
  }

  @GET
  @RolesAllowed("ADMIN")
  @Path("email")
  public String postGreeting(@Auth Optional<User> userOpt) {
    final String email = userOpt.flatMap(User::getEmail).orElse("e-mail address unknown");
    return String.format("Email: %s\n", email);
  }
}
