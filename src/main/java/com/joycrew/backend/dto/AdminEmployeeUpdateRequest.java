package com.joycrew.backend.dto;

import com.joycrew.backend.entity.enums.AdminLevel;

public record AdminEmployeeUpdateRequest(
        String name,
        Long departmentId,
        String position,
        AdminLevel level,
        String status
) {}