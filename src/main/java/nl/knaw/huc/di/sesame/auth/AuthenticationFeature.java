package nl.knaw.huc.di.sesame.auth;

import com.google.common.collect.ImmutableList;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.chained.ChainedAuthFilter;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.setup.Environment;
import nl.knaw.huc.di.sesame.SesameConfiguration;
import nl.knaw.huc.di.sesame.auth.basic.BasicAuthenticator;
import nl.knaw.huc.di.sesame.auth.google.GoogleAuthFilter;
import nl.knaw.huc.di.sesame.auth.google.GoogleAuthenticator;
import nl.knaw.huc.di.sesame.auth.huygens.HuygensAuthFilter;
import nl.knaw.huc.di.sesame.auth.huygens.HuygensAuthenticator;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.ServiceLocatorProvider;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import java.util.List;

public class AuthenticationFeature implements Feature {

  private final SesameConfiguration configuration;
  private final Environment environment;
  private final SessionManager sessionManager;
  private final String name;
  private final DefaultAuthorizer authorizer;

  public AuthenticationFeature(SesameConfiguration configuration, Environment environment,
                               SessionManager sessionManager, String name) {
    this.configuration = configuration;
    this.environment = environment;
    this.sessionManager = sessionManager;
    this.name = name;

    authorizer = new DefaultAuthorizer(configuration.getLocalAddresses());
  }

  @Override
  public boolean configure(FeatureContext context) {
    final ServiceLocator serviceLocator = ServiceLocatorProvider.getServiceLocator(context);

    final List<AuthFilter> authFilters = ImmutableList.of(
      createBasicCredentialAuthFilter(),
      createHuygensCredentialAuthFilter(serviceLocator),
      createGoogleCredentialAuthFilter());

    context.register(new AuthDynamicFeature(new ChainedAuthFilter<>(authFilters)));

    return true;
  }

  private BasicCredentialAuthFilter<User> createBasicCredentialAuthFilter() {
    return new BasicCredentialAuthFilter.Builder<User>()
      .setAuthenticator(new BasicAuthenticator())
      .setAuthorizer(authorizer)
      .setRealm("SECRET COW LEVEL")
      .buildAuthFilter();
  }

  private HuygensAuthFilter<User> createHuygensCredentialAuthFilter(ServiceLocator serviceLocator) {
    return new HuygensAuthFilter.Builder<User>()
      .setPrefix("Huygens")
      .setRealm("Federated Login")
      .setAuthenticator(createHuygensAuthenticator(serviceLocator))
      .setAuthorizer(authorizer)
      .buildAuthFilter();
  }

  private HuygensAuthenticator createHuygensAuthenticator(ServiceLocator serviceLocator) {
    final Client client = createClient(environment, configuration.getJerseyClientConfiguration());
    final HuygensAuthenticator authenticator = new HuygensAuthenticator(client, configuration.getFederatedAuthConfig());
    serviceLocator.inject(authenticator); // injects HttpServletRequest via @Context
    return authenticator;
  }

  private GoogleAuthFilter<User> createGoogleCredentialAuthFilter() {
    return new GoogleAuthFilter.Builder<User>()
      .setPrefix("Google")
      .setRealm("Google OAuth Login")
      .setAuthenticator(new GoogleAuthenticator(sessionManager))
      .setAuthorizer(authorizer)
      .buildAuthFilter();
  }

  private Client createClient(Environment environment, JerseyClientConfiguration configuration) {
    return new JerseyClientBuilder(environment).using(configuration).build(name);
  }
}
