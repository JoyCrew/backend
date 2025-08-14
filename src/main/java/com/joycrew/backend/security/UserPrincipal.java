package com.joycrew.backend.security;

import com.joycrew.backend.entity.Employee;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class UserPrincipal implements UserDetails {

  private final Employee employee;

  public UserPrincipal(Employee employee) {
    this.employee = employee;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + employee.getRole().name()));
  }

  @Override
  public String getPassword() {
    return employee.getPasswordHash();
  }

  @Override
  public String getUsername() {
    return employee.getEmail();
  }

  @Override
  public boolean isEnabled() {
    return employee.isActive();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }
}