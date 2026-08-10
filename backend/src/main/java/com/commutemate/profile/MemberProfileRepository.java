package com.commutemate.profile;
import java.util.Optional; import java.util.UUID; import org.springframework.data.jpa.repository.JpaRepository;
public interface MemberProfileRepository extends JpaRepository<MemberProfile,UUID>{ Optional<MemberProfile> findByTenantIdAndUserId(UUID tenantId,UUID userId); }
