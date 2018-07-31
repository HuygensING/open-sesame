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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static javax.ws.rs.core.HttpHeaders.WWW_AUTHENTICATE;
import static nl.knaw.huc.di.sesame.resources.GoogleLogin.RESOURCE_NAME;

@Path(RESOURCE_NAME)
public class GoogleLogin {
  static final String RESOURCE_NAME = "google";

  private static final String LOGIN_PATH = "login";
  private static final String CALLBACK_PATH = "oauth2";
  private static final String WWW_AUTH_BEARER_AND_REALM = "DI Huc realm=Google Login";

  // The *exact* redirect URI name MUST be Registered @Google Dashboard for the Client ID in use
  private static final String REDIRECT_URI = "http://localhost:8080/api/" + RESOURCE_NAME + "/" + CALLBACK_PATH;
  private static final Logger LOG = LoggerFactory.getLogger(GoogleLogin.class);

  private final GoogleAuthorizationCodeFlow flow;
  private final OAuth2Builder oAuth2Builder;
  private final SessionManager sessionManager;
  private final Map<UUID, String> returnURLs;
  private final boolean usePersistentCredentialStore;

  @Context
  private HttpServletRequest servletRequest;

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

    returnURLs = new ConcurrentHashMap<>();
  }

  @GET
  @Path(LOGIN_PATH)
  public Response login(@QueryParam("returnURL") String returnURL) {
    // Use this to carry over state from login to callback (session / userId / ...)
    final UUID sessionId = UUID.randomUUID();
    LOG.trace("Created sessionId: {}", sessionId);

    if (returnURL != null) {
      try {
        new URI(returnURL); // just check if it parses
      } catch (URISyntaxException e) {
        LOG.warn("Bad returnURL: {}", returnURL);
        throw new BadRequestException("Invalid returnURL: " + returnURL);
      }

      returnURLs.put(sessionId, returnURL);
    }

    final AuthorizationCodeRequestUrl url = flow.newAuthorizationUrl()
                                                .setState(sessionId.toString())
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
      return unauthorized("Missing required 'code' parameter");
    }

    if (state == null) {
      return unauthorized("Missing required 'state' parameter");
    }

    final UUID sessionId;
    try {
      sessionId = UUID.fromString(state);
    } catch (IllegalArgumentException e) {
      LOG.warn("Invalid UUID in state: {}", state);
      return unauthorized(String.format("Parameter 'state' is not a valid UUID: %s", state));
    }

    final Optional<Userinfo> optionalUserInfo = authorize(code).map(oAuth2Builder::build).map(Oauth2::userinfo);
    if (optionalUserInfo.isPresent()) {
      final Userinfo userInfo = optionalUserInfo.get();
      LOG.trace("userInfo: {}", userInfo);

      final Userinfoplus userinfoplus = userInfo.get().execute();
      LOG.trace("Google says: userinfoplus={}", userinfoplus.toPrettyString());

      final User user = createUser(userinfoplus);
      sessionManager.register(sessionId, user);

      final String returnURL = returnURLs.remove(sessionId);
      if (returnURL != null) {
        final URI location = URI.create(String.format("%s?gsid=%s", returnURL, sessionId));
        LOG.trace("returning to: {}", location);
        return Response.seeOther(location).build();
      }

      final String message = String.format("{\"sessionId\": \"%s\"}", sessionId);
      return ok(message);
    }

    return unauthorized("Unable to verify auth token: " + code);
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

  private User createUser(Userinfoplus userinfoplus) {
    return User.Builder.fromName(userinfoplus.getName())
                       .identifiedBy(userinfoplus.getId())
                       .withEmail(userinfoplus.getEmail())
                       .withRemoteAddr(servletRequest.getRemoteAddr())
                       .build();
  }

  private Response ok(String message) {
    return Response.ok(String.format("{\"ok\":%s}", message))
                   .build();
  }

  private Response unauthorized(String message) {
    return Response.status(Response.Status.UNAUTHORIZED)
                   .entity(String.format("{\"error\":\"%s\"}", message))
                   .header(WWW_AUTHENTICATE, WWW_AUTH_BEARER_AND_REALM)
                   .build();
  }
}
