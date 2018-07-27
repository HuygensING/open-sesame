package nl.knaw.huc.di.sesame.auth;

import com.google.common.base.MoreObjects;

import java.security.Principal;
import java.util.Objects;
import java.util.Optional;

public class User implements Principal {
  private final String name;

  private Optional<String> email;
  private Optional<String> host;
  private Optional<String> persistentId;

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

  public Optional<String> getHost() {
    return host;
  }

  public Optional<String> getPersistentId() {
    return persistentId;
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
      Objects.equals(host, user.host) &&
      Objects.equals(persistentId, user.persistentId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, email, host, persistentId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
                      .add("name", name)
                      .add("email", email)
                      .add("host", host)
                      .add("persistentId", persistentId)
                      .toString();
  }

  public static class Builder {
    private final String name;
    private String id;
    private String email;
    private String host;

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

    public Builder fromHost(String host) {
      this.host = host;
      return this;
    }

    public User build() {
      final User user = new User(name);
      user.persistentId = Optional.ofNullable(id);
      user.email = Optional.ofNullable(email);
      user.host = Optional.ofNullable(host);
      return user;
    }
  }
}
