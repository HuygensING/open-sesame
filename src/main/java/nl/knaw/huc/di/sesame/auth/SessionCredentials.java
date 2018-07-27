package nl.knaw.huc.di.sesame.auth;

import com.google.common.base.MoreObjects;

import java.util.UUID;

public class SessionCredentials {
  private final UUID sessionId;
  private final String host;

  public SessionCredentials(UUID sessionId, String host) {
    this.sessionId = sessionId;
    this.host = host;
  }

  public String getHost() {
    return host;
  }

  public UUID getSessionId() {
    return sessionId;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
                      .add("sessionId", sessionId)
                      .add("host", host)
                      .toString();
  }
}
