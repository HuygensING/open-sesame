package nl.knaw.huc.di.sesame.auth.google;

import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import nl.knaw.huc.di.sesame.auth.SessionManager;
import nl.knaw.huc.di.sesame.auth.User;

import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class GoogleAuthenticator implements Authenticator<UUID, User> {
  private final SessionManager sessionManager;

  public GoogleAuthenticator(SessionManager sessionManager) {
    this.sessionManager = checkNotNull(sessionManager);
  }

  @Override
  public Optional<User> authenticate(UUID credentials) throws AuthenticationException {
    return sessionManager.find(credentials);
  }
}
