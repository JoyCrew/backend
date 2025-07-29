package com.joycrew.backend.repository;

import com.joycrew.backend.entity.Employee;

import java.util.List;

public interface EmployeeQueryRepository {
    List<Employee> searchByKeyword(String keyword, int offset, int limit);
}
