package com.technova.campusdesk.system.controller;

import com.technova.campusdesk.system.dto.SystemStatusResponse;
import com.technova.campusdesk.system.service.SystemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Runtime environment diagnostics, managed entirely by Spring Boot.
 * Initial accounts come from DataInitializer and database/seed.sql, never from an endpoint.
 */
@RestController
@RequestMapping("/api/system")
public class SystemController {

  private final SystemService systemService;

  /** Spring only injects constructor dependencies. */
  public SystemController(SystemService systemService) {
    this.systemService = systemService;
  }

  /**
   * Prerequisite snapshot: database, schema, JWT and row counts.
   * Public so a fresh setup can be diagnosed before logging in.
   */
  @GetMapping("/status")
  @SecurityRequirements // removes the Bearer lock in Swagger
  @Operation(summary = "Environment status (database, schema, JWT, counts)")
  public SystemStatusResponse status() {
    return systemService.status();
  }
}
