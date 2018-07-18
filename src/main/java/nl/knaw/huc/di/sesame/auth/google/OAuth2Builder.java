package nl.knaw.huc.di.sesame.auth.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.oauth2.Oauth2;

public class OAuth2Builder {
  private final HttpTransport httpTransport;
  private final JsonFactory jsonFactory;
  private final String applicationName;

  public OAuth2Builder(HttpTransport httpTransport, JsonFactory jsonFactory, String applicationName) {
    this.httpTransport = httpTransport;
    this.jsonFactory = jsonFactory;
    this.applicationName = applicationName;
  }

  public Oauth2 build(Credential credential) {
    return new Oauth2.Builder(httpTransport, jsonFactory, credential)
      .setApplicationName(applicationName)
      .build();
  }
}
