package com.commutemate.identity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MembershipRepository extends JpaRepository<Membership, UUID> {
  Optional<Membership> findByOrganization_SlugAndUser_EmailIgnoreCase(String tenantSlug, String email);
  boolean existsByOrganization_IdAndUser_Id(UUID tenantId, UUID userId);
}
