package nl.knaw.huc.di.sesame.auth.huygens;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

import java.util.UUID;

/** Holds fields coming from the Huygens Security server */
public class HuygensDetails {
  @JsonProperty
  private UUID id;

  @JsonProperty
  private Owner owner;

  public UUID getId() {
    return id;
  }

  public Owner getOwner() {
    return owner;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
                      .add("id", id)
                      .add("owner", owner)
                      .toString();
  }

  @JsonIgnoreProperties({"commonName", "givenName", "surname", "affiliations", "organization"})
  public class Owner {
    @JsonProperty
    private String displayName;

    @JsonProperty
    private String emailAddress;

    @JsonProperty
    private String persistentID;

    public String getDisplayName() {
      return displayName;
    }

    public String getEmailAddress() {
      return emailAddress;
    }

    public String getPersistentID() {
      return persistentID;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
                        .add("displayName", displayName)
                        .add("emailAddress", emailAddress)
                        .add("persistentID", persistentID)
                        .toString();
    }
  }
}
