package nl.knaw.huc.di.sesame;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow.Builder;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
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
import nl.knaw.huc.di.sesame.auth.SessionManager;
import nl.knaw.huc.di.sesame.auth.User;
import nl.knaw.huc.di.sesame.auth.basic.BasicAuthenticator;
import nl.knaw.huc.di.sesame.auth.basic.BasicAuthorizer;
import nl.knaw.huc.di.sesame.auth.google.GoogleAuthFilter;
import nl.knaw.huc.di.sesame.auth.google.GoogleAuthenticator;
import nl.knaw.huc.di.sesame.auth.google.OAuth2Builder;
import nl.knaw.huc.di.sesame.auth.huygens.HuygensAuthFilter;
import nl.knaw.huc.di.sesame.auth.huygens.HuygensAuthenticator;
import nl.knaw.huc.di.sesame.resources.GoogleLogin;
import nl.knaw.huc.di.sesame.resources.Protected;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;

import java.util.List;

public class SesameApplication extends Application<SesameConfiguration> {
  private static final Logger LOG = LoggerFactory.getLogger(SesameApplication.class);

  private static final String APPLICATION_NAME = "Open Sesame";

  private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  // private static final File DATA_STORE_DIR = new File("/path/to/persistent/storage/dir");

  private static final List<String> SCOPES = ImmutableList.of(
    "https://www.googleapis.com/auth/userinfo.profile",
    "https://www.googleapis.com/auth/userinfo.email"
  );

  private static HttpTransport httpTransport;

  private final SessionManager sessionManager = new SessionManager();

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
    final JerseyEnvironment jersey = environment.jersey();
    jersey.register(new AuthDynamicFeature(new ChainedAuthFilter<>(createAuthFilters(configuration, environment))));
    jersey.register(RolesAllowedDynamicFeature.class);

    // So we can use @Auth to inject a custom Principal type into our resources
    jersey.register(new AuthValueFactoryProvider.Binder<>(User.class));
  }

  private ImmutableList<AuthFilter> createAuthFilters(SesameConfiguration configuration, Environment environment) {
    return ImmutableList.of(
      createBasicCredentialAuthFilter(),
      createHuygensCredentialAuthFilter(configuration, environment),
      createGoogleCredentialAuthFilter());
  }

  private GoogleAuthFilter<User> createGoogleCredentialAuthFilter() {
    return new GoogleAuthFilter.Builder<User>()
      .setPrefix("Google")
      .setRealm("Google OAuth Login")
      .setAuthenticator(new GoogleAuthenticator(sessionManager))
      .setAuthorizer(new BasicAuthorizer())
      .buildAuthFilter();
  }

  private HuygensAuthFilter<User> createHuygensCredentialAuthFilter(SesameConfiguration configuration,
                                                                    Environment environment) {
    final Client client = createClient(environment, configuration.getJerseyClientConfiguration());

    return new HuygensAuthFilter.Builder<User>()
      .setPrefix("Huygens")
      .setRealm("Federated Login")
      .setAuthenticator(new HuygensAuthenticator(client, configuration.getFederatedAuthConfig()))
      .setAuthorizer(new BasicAuthorizer())
      .buildAuthFilter();
  }

  private BasicCredentialAuthFilter<User> createBasicCredentialAuthFilter() {
    return new BasicCredentialAuthFilter.Builder<User>()
      .setAuthenticator(new BasicAuthenticator())
      .setAuthorizer(new BasicAuthorizer())
      .setRealm("SECRET COW LEVEL")
      .buildAuthFilter();
  }

  private void registerResources(SesameConfiguration configuration, JerseyEnvironment jersey) {
    jersey.register(createGoogleLoginResource(configuration.getGoogleConfig()));
    jersey.register(Protected.class);
  }

  private GoogleLogin createGoogleLoginResource(GoogleConfig googleConfig) {
    return new GoogleLogin(createFlow(googleConfig), createOAuth2Builder(), sessionManager);
  }

  private GoogleAuthorizationCodeFlow createFlow(GoogleConfig googleConfig) {
    final String clientId = googleConfig.getClientId();
    final String clientSecret = googleConfig.getClientSecret();
    final Builder codeFlowBuilder = new Builder(httpTransport, JSON_FACTORY, clientId, clientSecret, SCOPES);

    // If a persistent data store is desired, this may serve as an example.
    // LOG.trace("DATA_STORE_DIR: {}", DATA_STORE_DIR);
    // return codeFlowBuilder.setDataStoreFactory(new FileDataStoreFactory(DATA_STORE_DIR)).build();

    return codeFlowBuilder.build();
  }

  private OAuth2Builder createOAuth2Builder() {
    return new OAuth2Builder(httpTransport, JSON_FACTORY, getName());
  }

  private Client createClient(Environment environment, JerseyClientConfiguration configuration) {
    return new JerseyClientBuilder(environment).using(configuration).build(getName());
  }

}
