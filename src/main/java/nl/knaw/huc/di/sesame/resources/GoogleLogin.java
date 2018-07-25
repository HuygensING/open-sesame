package nl.knaw.huc.di.sesame.resources;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.Oauth2.Userinfo;
import com.google.api.services.oauth2.model.Userinfoplus;
import nl.knaw.huc.di.sesame.auth.SessionManager;
import nl.knaw.huc.di.sesame.auth.User;
import nl.knaw.huc.di.sesame.auth.google.OAuth2Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static javax.ws.rs.core.HttpHeaders.WWW_AUTHENTICATE;
import static nl.knaw.huc.di.sesame.resources.GoogleLogin.RESOURCE_NAME;

@Path(RESOURCE_NAME)
public class GoogleLogin {
  static final String RESOURCE_NAME = "google";

  private static final String LOGIN_PATH = "login";
  private static final String CALLBACK_PATH = "oauth2";
  private static final String WWW_AUTH_BEARER_AND_REALM = "DI Huc realm=Google Login";

  // The *exact* redirect URI name MUST be Registered @Google Dashboard for this Client ID
  private static final String REDIRECT_URI = "http://localhost:8080/" + RESOURCE_NAME + "/" + CALLBACK_PATH;
  private static final Logger LOG = LoggerFactory.getLogger(GoogleLogin.class);

  private final GoogleAuthorizationCodeFlow flow;
  private final OAuth2Builder oAuth2Builder;
  private final SessionManager sessionManager;
  private final boolean usePersistentCredentialStore;

  public GoogleLogin(GoogleAuthorizationCodeFlow flow, OAuth2Builder oAuth2Builder, SessionManager sessionManager) {
    this(flow, oAuth2Builder, sessionManager, false);
  }

  public GoogleLogin(GoogleAuthorizationCodeFlow flow, OAuth2Builder oAuth2Builder,
                     SessionManager sessionManager,
                     boolean usePersistentCredentialStore) {
    this.flow = flow;
    this.oAuth2Builder = oAuth2Builder;
    this.sessionManager = sessionManager;
    this.usePersistentCredentialStore = usePersistentCredentialStore;
  }

  @GET
  @Path(LOGIN_PATH)
  public Response login() {
    // Use this to carry over state from login to callback (session / userId / ...)
    final String sessionId = UUID.randomUUID().toString();
    LOG.trace("Created sessionId: {}", sessionId);

    final AuthorizationCodeRequestUrl url = flow.newAuthorizationUrl()
                                                .setState(sessionId)
                                                .setRedirectUri(REDIRECT_URI);

    return Response.temporaryRedirect(URI.create(url.build())).build();
  }

  @GET
  @Path(CALLBACK_PATH)
  @Produces(MediaType.APPLICATION_JSON)
  public Response googleCallback(@QueryParam("code") String code,
                                 @QueryParam("state") String state) throws IOException {

    LOG.trace("googleCallback, state=[{}], code=[{}]", state, code);

    if (code == null) {
      throw new NotAuthorizedException("Missing OAuth2 code param", WWW_AUTH_BEARER_AND_REALM);
    }

    final UUID sessionId;
    try {
      sessionId = UUID.fromString(state);
    } catch (IllegalArgumentException e) {
      LOG.warn("Illegal UUID in state: {}", state);
      throw new NotAuthorizedException("Malformed sessionId", WWW_AUTH_BEARER_AND_REALM);
    }

    final Optional<Userinfo> optionalUserInfo = authorize(code).map(oAuth2Builder::build).map(Oauth2::userinfo);
    if (optionalUserInfo.isPresent()) {
      final Userinfo userInfo = optionalUserInfo.get();
      LOG.trace("userInfo: {}", userInfo);

      final Userinfoplus userinfoplus = userInfo.get().execute();
      final User user = User.Builder.fromName(userinfoplus.getName())
                                    .identifiedBy(userinfoplus.getId())
                                    .withEmail(userinfoplus.getEmail())
                                    .build();

      sessionManager.register(sessionId, user);

      final String msg = String.format("{\"sessionId\": \"%s\",%s\"userInfo\": %s}", sessionId,
        System.lineSeparator(), userinfoplus.toPrettyString());
      LOG.trace("msg: {}", msg);

      return Response.ok(msg).build();
    }

    return Response.status(Response.Status.UNAUTHORIZED)
                   .entity("Unable to verify auth token " + code)
                   .header(WWW_AUTHENTICATE, WWW_AUTH_BEARER_AND_REALM)
                   .build();
  }

  private Optional<Credential> authorize(String code) throws IOException {
    final String userId = "user"; // or the userId used in the Credential Store

    if (usePersistentCredentialStore) {
      Credential credential = flow.loadCredential(userId);
      LOG.debug("loadCredential: {}", credential);
      if (credential != null
        && (credential.getRefreshToken() != null ||
        credential.getExpiresInSeconds() == null ||
        credential.getExpiresInSeconds() > 60)) {
        return Optional.of(credential);
      }
    }

    final TokenResponse response;
    try {
      response = flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute();
      LOG.debug("response: {}", response);
    } catch (Exception e) {
      LOG.warn("Exception while trying to authenticate using code {}: {}", code, e.getMessage());
      return Optional.empty(); // keep the reason to ourselves, just flag that we failed
    }

    // store credential and return it
    return Optional.of(flow.createAndStoreCredential(response, usePersistentCredentialStore ? userId : null));
  }
}
