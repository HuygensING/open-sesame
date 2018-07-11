package nl.knaw.huc.di.resources;

import io.dropwizard.auth.Auth;
import nl.knaw.huc.di.auth.User;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import java.util.Optional;

@Path("protected")
public class Protected {
  @GET
  public String getGreeting(@Auth Optional<User> userOpt) {
    final String name = userOpt.map(User::getName).orElse("anonymous user");
    return "Hello, " + name;
  }
}
