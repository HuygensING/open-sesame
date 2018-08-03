package nl.knaw.huc.di.sesame.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkNotNull;

public class SessionManager {
  private static final Logger LOG = LoggerFactory.getLogger(SessionManager.class);

  private final Map<UUID, User> sessions;

  public SessionManager() {
    sessions = new ConcurrentHashMap<>();
  }

  public Optional<User> find(UUID sessionId) {
    return Optional.ofNullable(sessions.get(checkNotNull(sessionId)));
  }

  public void register(UUID sessionId, User user) {
    LOG.trace("Registering session: {} -> {}", sessionId, user);
    sessions.put(checkNotNull(sessionId), checkNotNull(user));
  }

  public boolean delete(UUID sessionId) {
    return sessions.remove(checkNotNull(sessionId)) != null;
  }
}
