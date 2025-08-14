package com.joycrew.backend.config;

import com.joycrew.backend.entity.Employee;
import com.joycrew.backend.entity.enums.AdminLevel;
import com.joycrew.backend.security.UserPrincipal;
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
    users.put("testuser@joycrew.com", Employee.builder()
        .employeeId(1L)
        .email("testuser@joycrew.com")
        .employeeName("Test User")
        .role(AdminLevel.EMPLOYEE)
        .status("ACTIVE")
        .passwordHash("{noop}password")
        .build());

    users.put("nowallet@joycrew.com", Employee.builder()
        .employeeId(99L)
        .email("nowallet@joycrew.com")
        .employeeName("No Wallet User")
        .role(AdminLevel.EMPLOYEE)
        .status("ACTIVE")
        .passwordHash("{noop}password")
        .build());
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    if (!users.containsKey(username)) {
      throw new UsernameNotFoundException("User not found: " + username);
    }
    return new UserPrincipal(users.get(username));
  }
}