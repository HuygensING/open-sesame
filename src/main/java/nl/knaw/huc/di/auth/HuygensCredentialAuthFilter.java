package nl.knaw.huc.di.auth;

import io.dropwizard.auth.AuthFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;

import java.io.IOException;
import java.security.Principal;

public class HuygensCredentialAuthFilter<P extends Principal> extends AuthFilter<User, P> {
  private static final Logger LOG = LoggerFactory.getLogger(HuygensCredentialAuthFilter.class);

  private HuygensCredentialAuthFilter() {
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    User user = new User("Alibabba");
    LOG.debug("");
    if (!authenticate(requestContext, user, "HUYGENS")) {
      throw new WebApplicationException(unauthorizedHandler.buildResponse(prefix, realm));
    }
  }

  public static class Builder<P extends Principal> extends
    AuthFilterBuilder<User, P, HuygensCredentialAuthFilter<P>> {

    @Override
    protected HuygensCredentialAuthFilter<P> newInstance() {
      return new HuygensCredentialAuthFilter<>();
    }
  }
}
