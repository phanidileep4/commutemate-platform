package com.commutemate.tenant;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "organizations")
public class Organization {
  @Id @GeneratedValue private UUID id;
  @Column(nullable=false, unique=true) private String slug;
  @Column(nullable=false) private String name;
  @Column(nullable=false) private String status = "ACTIVE";
  @Column(name="created_at", nullable=false) private Instant createdAt = Instant.now();
  protected Organization() {}
  public Organization(String slug, String name) { this.slug = slug; this.name = name; }
  public UUID getId() { return id; }
  public String getSlug() { return slug; }
  public String getName() { return name; }
  public String getStatus() { return status; }
}
