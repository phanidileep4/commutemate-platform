package com.commutemate.profile;

import com.commutemate.common.TenantContextHolder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/profile")
public class ProfileController {
  private final MemberProfileRepository profiles; public ProfileController(MemberProfileRepository profiles){this.profiles=profiles;}
  public record UpdateProfileRequest(String originGeohash,@Min(0) @Max(100) short conversationPref,@Min(0) @Max(100) short musicPref,@Min(0) @Max(100) short punctualityPref,@Min(0) @Max(100) short networkingPref,@Min(0) @Max(100) short varietyPref,boolean driverEnabled,@Min(0) @Max(8) short seats){}
  @GetMapping public MemberProfile get(){var c=TenantContextHolder.getRequired(); return profiles.findByTenantIdAndUserId(c.tenantId(),c.userId()).orElseGet(()->new MemberProfile(c.tenantId(),c.userId()));}
  @PutMapping @Transactional public MemberProfile put(@Valid @RequestBody UpdateProfileRequest r){var c=TenantContextHolder.getRequired(); var p=profiles.findByTenantIdAndUserId(c.tenantId(),c.userId()).orElseGet(()->new MemberProfile(c.tenantId(),c.userId())); p.update(r.originGeohash(),r.conversationPref(),r.musicPref(),r.punctualityPref(),r.networkingPref(),r.varietyPref(),r.driverEnabled(),r.seats()); return profiles.save(p);}
}
