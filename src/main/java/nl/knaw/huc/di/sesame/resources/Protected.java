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
  public String getEmail(@Auth Optional<User> userOpt) {
    final String email = userOpt.flatMap(User::getEmail).orElse("e-mail address unknown");
    final boolean isAccessingLocalhost = userOpt.flatMap(User::getHost).map("localhost"::equals).orElse(false);
    return String.format("Email: %s\nLocal: %s\n", email, isAccessingLocalhost);
  }

  @GET
  @RolesAllowed("LOCAL")
  @Path("local")
  public String fromLocalOnly(@Auth Optional<User> userOpt) {
    return String.format("Host: %s\n", userOpt.flatMap(User::getHost).orElse("unknown"));
  }
}
