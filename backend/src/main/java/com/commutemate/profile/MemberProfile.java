package com.commutemate.profile;

import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name="member_profiles", uniqueConstraints=@UniqueConstraint(columnNames={"tenant_id","user_id"}))
public class MemberProfile {
  @Id @GeneratedValue private UUID id;
  @Column(name="tenant_id",nullable=false) private UUID tenantId;
  @Column(name="user_id",nullable=false) private UUID userId;
  @Column(name="origin_geohash") private String originGeohash;
  @Column(name="conversation_pref",nullable=false) private short conversationPref=50;
  @Column(name="music_pref",nullable=false) private short musicPref=50;
  @Column(name="punctuality_pref",nullable=false) private short punctualityPref=80;
  @Column(name="networking_pref",nullable=false) private short networkingPref=30;
  @Column(name="variety_pref",nullable=false) private short varietyPref=50;
  @Column(name="driver_enabled",nullable=false) private boolean driverEnabled=false;
  @Column(nullable=false) private short seats=0;
  protected MemberProfile(){}
  public MemberProfile(UUID tenantId,UUID userId){this.tenantId=tenantId;this.userId=userId;}
  public void update(String originGeohash,short conversation,short music,short punctuality,short networking,short variety,boolean driverEnabled,short seats){
    this.originGeohash=originGeohash;this.conversationPref=conversation;this.musicPref=music;this.punctualityPref=punctuality;this.networkingPref=networking;this.varietyPref=variety;this.driverEnabled=driverEnabled;this.seats=seats;
  }
  public UUID getId(){return id;} public UUID getTenantId(){return tenantId;} public UUID getUserId(){return userId;} public String getOriginGeohash(){return originGeohash;} public short getConversationPref(){return conversationPref;} public short getMusicPref(){return musicPref;} public short getPunctualityPref(){return punctualityPref;} public short getNetworkingPref(){return networkingPref;} public short getVarietyPref(){return varietyPref;} public boolean isDriverEnabled(){return driverEnabled;} public short getSeats(){return seats;}
}
