package nl.knaw.huc.di.sesame.resources;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.services.oauth2.Oauth2.Userinfo;
import com.google.api.services.oauth2.model.Userinfoplus;
import nl.knaw.huc.di.sesame.auth.google.OAuth2Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.net.URI;

import static nl.knaw.huc.di.sesame.resources.GoogleAuth.RESOURCE_NAME;

@Path(RESOURCE_NAME)
public class GoogleAuth {
  static final String RESOURCE_NAME = "google";

  private static final String CALLBACK_NAME = "oauth2";

  // The *exact* redirect URI name MUST be Registered @Google Dashboard for this Client ID
  private static final String REDIRECT_URI = "http://localhost:8080/" + RESOURCE_NAME + "/" + CALLBACK_NAME;

  private static final Logger LOG = LoggerFactory.getLogger(GoogleAuth.class);
  private final GoogleAuthorizationCodeFlow flow;
  private final OAuth2Builder oAuth2Builder;

  public GoogleAuth(GoogleAuthorizationCodeFlow flow, OAuth2Builder oAuth2Builder) {
    this.flow = flow;
    this.oAuth2Builder = oAuth2Builder;
  }

  @GET
  public Response login() {
    AuthorizationCodeRequestUrl url = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI);
    return Response.temporaryRedirect(URI.create(url.build())).build();
  }

  @GET
  @Path(CALLBACK_NAME)
  @Produces(MediaType.APPLICATION_JSON)
  public Response googleCallback(@QueryParam("code") String code) throws IOException {
    LOG.warn("googleCallback, code=[{}]", code);
    if (code == null) {
      return Response.noContent().build();
    }

    final Credential credential = authorize(code);

    final Userinfo userinfo = oAuth2Builder.build(credential).userinfo();
    LOG.trace("userinfo: {}", userinfo);

    final Userinfoplus userinfoplus = userinfo.get().execute();
    return Response.ok(userinfoplus.toPrettyString()).build();
  }

  private Credential authorize(String code) throws IOException {
    final String userId = "user";

    Credential credential = flow.loadCredential(userId);
    LOG.debug("loadCredential: {}", credential);
    if (credential != null
      && (credential.getRefreshToken() != null ||
      credential.getExpiresInSeconds() == null ||
      credential.getExpiresInSeconds() > 60)) {
      return credential;
    }

    final TokenResponse response =
      flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute();
    LOG.debug("response: {}", response);

    // store credential and return it
    return flow.createAndStoreCredential(response, userId);
  }
}
