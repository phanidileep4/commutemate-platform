package com.commutemate.identity;

import com.commutemate.common.TenantContext;
import com.commutemate.common.TenantContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class DevIdentityFilter extends OncePerRequestFilter {
  private final MembershipRepository memberships;
  private final boolean enabled;
  public DevIdentityFilter(MembershipRepository memberships, @Value("${commutemate.security.dev-headers-enabled:true}") boolean enabled) {
    this.memberships = memberships; this.enabled = enabled;
  }
  @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
    try {
      if (enabled && req.getRequestURI().startsWith("/api/")) {
        String email = req.getHeader("X-User-Email");
        String tenant = req.getHeader("X-Tenant-Slug");
        if (email != null && tenant != null) {
          memberships.findByOrganization_SlugAndUser_EmailIgnoreCase(tenant, email).ifPresent(m -> TenantContextHolder.set(
              new TenantContext(m.getOrganization().getId(), m.getUser().getId(), m.getOrganization().getSlug(), m.getUser().getEmail(), m.getRole())));
        }
      }
      chain.doFilter(req, res);
    } finally { TenantContextHolder.clear(); }
  }
}
