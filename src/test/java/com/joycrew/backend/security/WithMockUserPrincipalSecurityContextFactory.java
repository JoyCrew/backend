package com.joycrew.backend.security;

import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.enums.AdminLevel;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockUserPrincipalSecurityContextFactory implements WithSecurityContextFactory<WithMockUserPrincipal> {
    @Override
    public SecurityContext createSecurityContext(WithMockUserPrincipal annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Employee mockEmployee = Employee.builder()
                .employeeId(annotation.id())
                .email(annotation.email())
                .employeeName("Test User")
                .role(AdminLevel.valueOf(annotation.role()))
                .passwordHash("mockPassword")
                .status("ACTIVE")
                .build();
        UserPrincipal principal = new UserPrincipal(mockEmployee);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal, principal.getPassword(), principal.getAuthorities());
        context.setAuthentication(authentication);
        return context;
    }
}