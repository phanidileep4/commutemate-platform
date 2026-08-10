package com.commutemate.tenant;

import com.commutemate.common.TenantContextHolder;
import com.commutemate.identity.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/platform/tenants")
public class TenantAdminController {
  private final OrganizationRepository organizations; private final UserAccountRepository users; private final MembershipRepository memberships; @Value("${commutemate.security.bootstrap-token:local-bootstrap-only}") private String bootstrapToken;
  public TenantAdminController(OrganizationRepository organizations, UserAccountRepository users, MembershipRepository memberships){this.organizations=organizations;this.users=users;this.memberships=memberships;}
  public record CreateTenantRequest(@NotBlank String slug,@NotBlank String name,@Email @NotBlank String adminEmail,@NotBlank String adminName){}
  @PostMapping @Transactional
  public Object create(@RequestHeader("X-Platform-Bootstrap-Token") String token,@Valid @RequestBody CreateTenantRequest r){
    if(!bootstrapToken.equals(token)) throw new SecurityException("invalid platform bootstrap token");
    var slug=r.slug().trim().toLowerCase();
    if(!slug.matches("[a-z0-9][a-z0-9-]{1,48}[a-z0-9]")) throw new IllegalArgumentException("slug must be 3-50 lowercase letters, numbers, or hyphens");
    if(organizations.findBySlug(slug).isPresent()) throw new IllegalArgumentException("tenant slug already exists");
    var org=organizations.save(new Organization(slug,r.name().trim()));
    var user=users.findByEmailIgnoreCase(r.adminEmail()).orElseGet(() -> users.save(new UserAccount(r.adminEmail(),r.adminName().trim())));
    memberships.save(new Membership(org,user,"TENANT_ADMIN"));
    return Map.of("tenantId",org.getId(),"slug",org.getSlug(),"adminUserId",user.getId());
  }

  @GetMapping("/current") public Object current(){ var c=TenantContextHolder.getRequired(); return organizations.findById(c.tenantId()).orElseThrow(); }
}
