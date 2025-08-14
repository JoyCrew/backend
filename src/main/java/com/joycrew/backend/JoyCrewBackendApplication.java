package com.joycrew.backend;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class JoyCrewBackendApplication {

  public static void main(String[] args) {
    SpringApplication.run(JoyCrewBackendApplication.class, args);
  }

  /**
   * Set the default timezone for the application to Korea Standard Time (KST).
   * This ensures all date and time operations are based on KST.
   */
  @PostConstruct
  public void init() {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
  }
}
