package com.joycrew.backend.service.mapper;

import com.joycrew.backend.dto.AdminEmployeeQueryResponse;
import com.joycrew.backend.dto.EmployeeQueryResponse;
import com.joycrew.backend.dto.PointBalanceResponse;
import com.joycrew.backend.dto.UserProfileResponse;
import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.Wallet;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {

    public EmployeeQueryResponse toEmployeeQueryResponse(Employee employee) {
        String departmentName = (employee.getDepartment() != null) ? employee.getDepartment().getName() : null;
        return new EmployeeQueryResponse(
                employee.getEmployeeId(),
                employee.getProfileImageUrl(),
                employee.getEmployeeName(),
                departmentName,
                employee.getPosition()
        );
    }

    public AdminEmployeeQueryResponse toAdminEmployeeQueryResponse(Employee employee) {
        String departmentName = (employee.getDepartment() != null) ? employee.getDepartment().getName() : null;
        return new AdminEmployeeQueryResponse(
                employee.getEmployeeId(),
                employee.getEmployeeName(),
                employee.getEmail(),
                departmentName,
                employee.getPosition(),
                employee.getProfileImageUrl(),
                employee.getRole().name(),
                employee.getBirthday(),
                employee.getAddress(),
                employee.getHireDate()
        );
    }

    public UserProfileResponse toUserProfileResponse(Employee employee, Wallet wallet) {
        String departmentName = employee.getDepartment() != null ? employee.getDepartment().getName() : null;
        return new UserProfileResponse(
                employee.getEmployeeId(),
                employee.getEmployeeName(),
                employee.getEmail(),
                employee.getProfileImageUrl(),
                wallet.getBalance(),
                wallet.getGiftablePoint(),
                employee.getRole(),
                departmentName,
                employee.getPosition(),
                employee.getBirthday(),
                employee.getAddress(),
                employee.getHireDate()
        );
    }

    public PointBalanceResponse toPointBalanceResponse(Wallet wallet) {
        return new PointBalanceResponse(wallet.getBalance(), wallet.getGiftablePoint());
    }
}