package nl.knaw.huc.di.sesame;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.common.collect.ImmutableList;
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
import nl.knaw.huc.di.sesame.SesameConfiguration.GoogleConfig;
import nl.knaw.huc.di.sesame.auth.ExampleAuthenticator;
import nl.knaw.huc.di.sesame.auth.ExampleAuthorizer;
import nl.knaw.huc.di.sesame.auth.HuygensAuthenticator;
import nl.knaw.huc.di.sesame.auth.HuygensCredentialAuthFilter;
import nl.knaw.huc.di.sesame.auth.User;
import nl.knaw.huc.di.sesame.auth.google.OAuth2Builder;
import nl.knaw.huc.di.sesame.resources.GoogleAuth;
import nl.knaw.huc.di.sesame.resources.Protected;
import org.assertj.core.util.Lists;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;

import java.io.File;
import java.util.List;

public class SesameApplication extends Application<SesameConfiguration> {
  private static final Logger LOG = LoggerFactory.getLogger(SesameApplication.class);

  private static final String APPLICATION_NAME = "Open Sesame";

  private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  private static final File DATA_STORE_DIR = new File(System.getProperty("user.home"), ".store/open_sesame");

  private static final List<String> SCOPES = ImmutableList.of(
    "https://www.googleapis.com/auth/userinfo.profile",
    "https://www.googleapis.com/auth/userinfo.email"
  );

  private static HttpTransport httpTransport;

  public static void main(String[] args) throws Exception {
    new SesameApplication().run(args);
  }

  @Override
  public String getName() {
    return APPLICATION_NAME;
  }

  @Override
  public void run(SesameConfiguration configuration, Environment environment) throws Exception {
    httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    registerAuth(configuration, environment);
    registerResources(configuration, environment.jersey());
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

  private void registerResources(SesameConfiguration configuration, JerseyEnvironment jersey) throws Exception {
    jersey.register(new GoogleAuth(createFlow(configuration.getGoogleConfig()), createOAuth2Builder()));
    jersey.register(Protected.class);
  }

  private GoogleAuthorizationCodeFlow createFlow(GoogleConfig googleConfig) throws Exception {
    LOG.trace("DATA_STORE_DIR: {}", DATA_STORE_DIR);

    final GoogleAuthorizationCodeFlow.Builder builder = new GoogleAuthorizationCodeFlow.Builder(
      httpTransport, JSON_FACTORY, googleConfig.getId(), googleConfig.getSecret(), SCOPES);

    return builder.setDataStoreFactory(new FileDataStoreFactory(DATA_STORE_DIR)).build();
  }

  private OAuth2Builder createOAuth2Builder() {
    return new OAuth2Builder(httpTransport, JSON_FACTORY, getName());
  }

  private Client createClient(Environment environment, JerseyClientConfiguration configuration) {
    return new JerseyClientBuilder(environment).using(configuration).build(getName());
  }
}
