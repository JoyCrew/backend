package com.joycrew.backend.dto;

import com.joycrew.backend.entity.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminEmployeeUpdateRequest {
    private String name;
    private Long departmentId;
    private String position;
    private UserRole role;
    private String status;
}
