package com.joycrew.backend.dto;

import com.joycrew.backend.entity.enums.UserRole;

public record AdminEmployeeUpdateRequest(
        String name,
        Long departmentId,
        String position,
        UserRole role,
        String status
) {}