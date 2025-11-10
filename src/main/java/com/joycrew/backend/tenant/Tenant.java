// src/main/java/com/joycrew/backend/tenant/Tenant.java
package com.joycrew.backend.tenant;

public final class Tenant {
    public static Long id() {
        Long id = TenantContext.get();
        if (id == null) throw new IllegalStateException("TenantContext is not set");
        return id;
    }
}

