package com.commutemate.common;

public final class TenantContextHolder {
  private static final ThreadLocal<TenantContext> CURRENT = new ThreadLocal<>();
  private TenantContextHolder() {}
  public static void set(TenantContext context) { CURRENT.set(context); }
  public static TenantContext getRequired() {
    var context = CURRENT.get();
    if (context == null) throw new IllegalStateException("No tenant context is active");
    return context;
  }
  public static void clear() { CURRENT.remove(); }
}
