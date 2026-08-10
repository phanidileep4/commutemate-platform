package com.commutemate.identity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="users")
public class UserAccount {
  @Id @GeneratedValue private UUID id;
  @Column(nullable=false, unique=true) private String email;
  @Column(name="display_name", nullable=false) private String displayName;
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
  protected UserAccount() {}
  public UserAccount(String email, String displayName) { this.email=email.toLowerCase(); this.displayName=displayName; }
  public UUID getId(){return id;} public String getEmail(){return email;} public String getDisplayName(){return displayName;}
}
