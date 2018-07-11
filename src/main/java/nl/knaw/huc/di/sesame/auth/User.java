package nl.knaw.huc.di.sesame.auth;

import com.google.common.base.MoreObjects;

import java.security.Principal;

public class User implements Principal {
  private final String name;

  User(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
                      .add("name", name)
                      .toString();
  }
}
