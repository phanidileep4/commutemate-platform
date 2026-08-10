package com.commutemate.ride; import java.time.Instant; import java.util.List; import java.util.UUID; import org.springframework.data.jpa.repository.JpaRepository;
public interface ConfirmedRideRepository extends JpaRepository<ConfirmedRide,UUID>{ List<ConfirmedRide> findAllByTenantIdAndDepartureAtAfterOrderByDepartureAt(UUID tenantId,Instant after); }
