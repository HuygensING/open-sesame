package nl.knaw.huc.di.sesame;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.cache.CacheBuilderSpec;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.util.List;

public class SesameConfiguration extends Configuration {
  private CacheBuilderSpec authenticationCachePolicy;

  @Valid
  @NotNull
  @JsonProperty("federatedAuthentication")
  private FederatedAuthConfig federatedAuthConfig;

  @Valid
  @NotNull
  @JsonProperty("google")
  private GoogleConfig googleConfig;

  @Valid
  @NotNull
  @JsonProperty("jerseyClient")
  private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

  @JsonProperty("localAddresses")
  private List<String> localAddresses;

  public CacheBuilderSpec getAuthenticationCachePolicy() {
    return authenticationCachePolicy;
  }

  public GoogleConfig getGoogleConfig() {
    return googleConfig;
  }

  public FederatedAuthConfig getFederatedAuthConfig() {
    return federatedAuthConfig;
  }

  public JerseyClientConfiguration getJerseyClientConfiguration() {
    return jerseyClient;
  }

  public List<String> getLocalAddresses() {
    return localAddresses;
  }

  public static class FederatedAuthConfig {
    @Valid
    @NotNull
    private String url;

    @Valid
    @NotNull
    private String credentials;

    @JsonProperty
    public String getUrl() {
      return url;
    }

    @JsonProperty
    public String getCredentials() {
      return credentials;
    }
  }

  public static class GoogleConfig {
    @Valid
    @NotNull
    String clientSecret;

    @Valid
    @NotNull
    private String clientId;

    @JsonProperty
    public String getClientId() {
      return clientId;
    }

    @JsonProperty
    public String getClientSecret() {
      return clientSecret;
    }
  }
}
