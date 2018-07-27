package nl.knaw.huc.di.sesame.auth;

import com.google.common.base.MoreObjects;

import java.security.Principal;
import java.util.Objects;
import java.util.Optional;

public class User implements Principal {
  private final String name;

  private Optional<String> email;
  private Optional<String> persistentId;
  private Optional<String> remoteAddr;

  private User(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  public Optional<String> getEmail() {
    return email;
  }

  public Optional<String> getPersistentId() {
    return persistentId;
  }

  public Optional<String> getRemoteAddr() {
    return remoteAddr;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    return Objects.equals(name, user.name) &&
      Objects.equals(email, user.email) &&
      Objects.equals(persistentId, user.persistentId) &&
      Objects.equals(remoteAddr, user.remoteAddr);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, email, persistentId, remoteAddr);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
                      .add("name", name)
                      .add("email", email)
                      .add("persistentId", persistentId)
                      .add("remoteAddr", remoteAddr)
                      .toString();
  }

  public static class Builder {
    private final String name;
    private String id;
    private String email;
    private String remoteAddr;

    private Builder(String name) {
      this.name = name;
    }

    public static Builder fromName(String name) {
      return new Builder(name);
    }

    public Builder identifiedBy(String id) {
      this.id = id;
      return this;
    }

    public Builder withEmail(String email) {
      this.email = email;
      return this;
    }

    public Builder withRemoteAddr(String remoteAddr) {
      this.remoteAddr = remoteAddr;
      return this;
    }

    public User build() {
      final User user = new User(name);
      user.persistentId = Optional.ofNullable(id);
      user.email = Optional.ofNullable(email);
      user.remoteAddr = Optional.ofNullable(remoteAddr);
      return user;
    }
  }
}
