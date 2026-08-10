package com.commutemate.common;

import java.util.UUID;

public record TenantContext(UUID tenantId, UUID userId, String tenantSlug, String email, String role) {}
