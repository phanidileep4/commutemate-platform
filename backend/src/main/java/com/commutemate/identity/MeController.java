package com.commutemate.identity;

import com.commutemate.common.TenantContextHolder;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController @RequestMapping("/api/v1")
public class MeController {
  @GetMapping("/me") Object me() {
    var c=TenantContextHolder.getRequired();
    return Map.of("userId",c.userId(),"email",c.email(),"tenantId",c.tenantId(),"tenantSlug",c.tenantSlug(),"role",c.role());
  }
  @GetMapping("/tenant") Object tenant() {
    var c=TenantContextHolder.getRequired(); return Map.of("id",c.tenantId(),"slug",c.tenantSlug());
  }
}
