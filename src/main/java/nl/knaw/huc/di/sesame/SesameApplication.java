package nl.knaw.huc.di.sesame;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.chained.ChainedAuthFilter;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;
import nl.knaw.huc.di.sesame.auth.ExampleAuthenticator;
import nl.knaw.huc.di.sesame.auth.ExampleAuthorizer;
import nl.knaw.huc.di.sesame.auth.HuygensAuthenticator;
import nl.knaw.huc.di.sesame.auth.HuygensCredentialAuthFilter;
import nl.knaw.huc.di.sesame.auth.User;
import nl.knaw.huc.di.sesame.resources.Protected;
import org.assertj.core.util.Lists;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;

import java.util.List;

public class SesameApplication extends Application<SesameConfiguration> {
  private static final Logger LOG = LoggerFactory.getLogger(SesameApplication.class);

  public static void main(String[] args) throws Exception {
    new SesameApplication().run(args);
  }

  @Override
  public void run(SesameConfiguration configuration, Environment environment) {
    registerAuth(configuration, environment);
    registerResources(environment.jersey());
  }

  private void registerAuth(SesameConfiguration configuration, Environment environment) {
    final AuthFilter basicCredentialAuthFilter = new BasicCredentialAuthFilter.Builder<User>()
      .setAuthenticator(new ExampleAuthenticator())
      .setAuthorizer(new ExampleAuthorizer())
      .setRealm("SECRET COW LEVEL")
      .buildAuthFilter();

    final Client client = createClient(environment, configuration.getJerseyClientConfiguration());
    final AuthFilter huygensCredentialAuthFilter = new HuygensCredentialAuthFilter.Builder<User>()
      .setPrefix("Huygens")
      .setRealm("Federated Authentication")
      .setAuthenticator(new HuygensAuthenticator(client, configuration.getFederatedAuthConfig()))
      .setAuthorizer(new ExampleAuthorizer())
      .buildAuthFilter();

    final JerseyEnvironment jersey = environment.jersey();
    final List<AuthFilter> filters = Lists.newArrayList(basicCredentialAuthFilter, huygensCredentialAuthFilter);
    jersey.register(new AuthDynamicFeature(new ChainedAuthFilter<>(filters)));
    jersey.register(RolesAllowedDynamicFeature.class);
    // So we can use @Auth to inject a custom Principal type into our resources
    jersey.register(new AuthValueFactoryProvider.Binder<>(User.class));
  }

  private void registerResources(JerseyEnvironment jersey) {
    jersey.register(Protected.class);
  }

  private Client createClient(Environment environment, JerseyClientConfiguration configuration) {
    return new JerseyClientBuilder(environment).using(configuration).build(getName());
  }
}
