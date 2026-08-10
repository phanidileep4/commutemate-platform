package com.commutemate.identity;

import com.commutemate.tenant.Organization;
import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name="memberships", uniqueConstraints=@UniqueConstraint(columnNames={"tenant_id","user_id"}))
public class Membership {
  @Id @GeneratedValue private UUID id;
  @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="tenant_id") private Organization organization;
  @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="user_id") private UserAccount user;
  @Column(nullable=false) private String role;
  @Column(name="external_subject") private String externalSubject;
  @Column(nullable=false) private String status="ACTIVE";
  protected Membership() {}
  public Membership(Organization organization, UserAccount user, String role) { this.organization=organization; this.user=user; this.role=role; }
  public UUID getId(){return id;} public Organization getOrganization(){return organization;} public UserAccount getUser(){return user;} public String getRole(){return role;} public String getStatus(){return status;}
}
