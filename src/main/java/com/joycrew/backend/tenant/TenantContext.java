// src/main/java/com/joycrew/backend/tenant/TenantContext.java
package com.joycrew.backend.tenant;

public final class TenantContext {
    private static final ThreadLocal<Long> CURRENT = new ThreadLocal<>();
    private TenantContext() {}

    public static void set(Long companyId) { CURRENT.set(companyId); }
    public static Long get() { return CURRENT.get(); }
    public static void clear() { CURRENT.remove(); }
}
