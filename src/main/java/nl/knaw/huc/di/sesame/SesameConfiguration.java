package nl.knaw.huc.di.sesame;

import com.google.common.cache.CacheBuilderSpec;
import io.dropwizard.Configuration;

public class SesameConfiguration extends Configuration {
  private CacheBuilderSpec authenticationCachePolicy;

  public CacheBuilderSpec getAuthenticationCachePolicy() {
    return authenticationCachePolicy;
  }
}
