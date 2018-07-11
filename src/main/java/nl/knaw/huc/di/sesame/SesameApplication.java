package nl.knaw.huc.di.sesame;

import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;
import nl.knaw.huc.di.auth.ExampleAuthenticator;
import nl.knaw.huc.di.auth.ExampleAuthorizer;
import nl.knaw.huc.di.auth.User;
import nl.knaw.huc.di.resources.Protected;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

public class SesameApplication extends Application<SesameConfiguration> {
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
    jersey.register(new AuthDynamicFeature(
      new BasicCredentialAuthFilter.Builder<User>()
        .setAuthenticator(new ExampleAuthenticator())
        .setAuthorizer(new ExampleAuthorizer())
        .setRealm("SECRET COW LEVEL")
        .buildAuthFilter()
    ));

    jersey.register(RolesAllowedDynamicFeature.class);

    // So we can use @Auth to inject a custom Principal type into our resources
    jersey.register(new AuthValueFactoryProvider.Binder<>(User.class));
  }

  private void registerResources(JerseyEnvironment jersey) {
    jersey.register(Protected.class);
  }
}
