package com.technova.campusdesk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

/** Excludes Spring's in-memory user: login uses DB + JWT instead. */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class CampusDeskApplication {

  /** Application entry point. */
  public static void main(String[] args) {
    SpringApplication.run(CampusDeskApplication.class, args);
  }
}
