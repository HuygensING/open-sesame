package nl.knaw.huc.di.sesame;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.chained.ChainedAuthFilter;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;
import nl.knaw.huc.di.auth.ExampleAuthenticator;
import nl.knaw.huc.di.auth.ExampleAuthorizer;
import nl.knaw.huc.di.auth.HuygensCredentialAuthFilter;
import nl.knaw.huc.di.auth.User;
import nl.knaw.huc.di.resources.Protected;
import org.assertj.core.util.Lists;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class SesameApplication extends Application<SesameConfiguration> {
  private static final Logger LOG = LoggerFactory.getLogger(SesameApplication.class);

  public static void main(String[] args) throws Exception {
    new SesameApplication().run(args);
  }

  @Override
  public void run(SesameConfiguration configuration, Environment environment) {
    final JerseyEnvironment jersey = environment.jersey();
    registerAuth(jersey);
    registerResources(jersey);
  }

  private void registerAuth(JerseyEnvironment jersey) {
    final AuthFilter basicCredentialAuthFilter = new BasicCredentialAuthFilter.Builder<User>()
      .setAuthenticator(new ExampleAuthenticator())
      .setAuthorizer(new ExampleAuthorizer())
      .setRealm("SECRET COW LEVEL")
      .buildAuthFilter();

    final AuthFilter huygensCredentialAuthFilter = new HuygensCredentialAuthFilter.Builder<User>()
      .setAuthenticator(credentials -> {
        LOG.info("Trying Huygens auth filter");
        return Optional.empty();
      })
      .buildAuthFilter();

    final List<AuthFilter> filters = Lists.newArrayList(basicCredentialAuthFilter, huygensCredentialAuthFilter);
    jersey.register(new AuthDynamicFeature(new ChainedAuthFilter<>(filters)));
    jersey.register(RolesAllowedDynamicFeature.class);
    // So we can use @Auth to inject a custom Principal type into our resources
    jersey.register(new AuthValueFactoryProvider.Binder<>(User.class));
  }

  private void registerResources(JerseyEnvironment jersey) {
    jersey.register(Protected.class);
  }
}
