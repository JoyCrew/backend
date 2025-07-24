package com.joycrew.backend.config;

import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.enums.UserRole;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TestUserDetailsService implements UserDetailsService {

    private final Map<String, Employee> users = new HashMap<>();

    public TestUserDetailsService() {
        // Pre-populate with test users
        users.put("testuser@joycrew.com", Employee.builder()
                .employeeId(1L)
                .email("testuser@joycrew.com")
                .employeeName("테스트유저")
                .role(UserRole.EMPLOYEE)
                .status("ACTIVE")
                .passwordHash("{noop}password") // Use {noop} for plain text password in tests or your actual encoder
                .build());

        users.put("nowallet@joycrew.com", Employee.builder()
                .employeeId(99L)
                .email("nowallet@joycrew.com")
                .employeeName("지갑없음")
                .role(UserRole.EMPLOYEE)
                .status("ACTIVE")
                .passwordHash("{noop}password")
                .build());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (!users.containsKey(username)) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        return users.get(username);
    }
}