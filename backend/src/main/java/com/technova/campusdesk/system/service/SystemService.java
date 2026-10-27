package com.technova.campusdesk.system.service;

import com.technova.campusdesk.config.StartupValidator;
import com.technova.campusdesk.system.dto.SystemStatusResponse;
import com.technova.campusdesk.ticket.repository.TicketRepository;
import com.technova.campusdesk.user.repository.UserRepository;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Environment diagnostics (GET /api/system/status). Read-only: never writes data. */
@Service
public class SystemService {

  private final UserRepository users;
  private final TicketRepository tickets;
  private final DataSource dataSource;
  private final String jwtSecret;

  /** Spring only injects constructor dependencies. */
  public SystemService(
      UserRepository users,
      TicketRepository tickets,
      DataSource dataSource,
      @Value("${app.jwt.secret:}") String jwtSecret) {
    this.users = users;
    this.tickets = tickets;
    this.dataSource = dataSource;
    this.jwtSecret = jwtSecret;
  }

  /** Current prerequisite state. Counts are -1 when they cannot be read. */
  @Transactional(readOnly = true)
  public SystemStatusResponse status() {
    boolean dbReachable = false;
    boolean schemaReady = false;
    long userCount = -1;
    long ticketCount = -1;
    try (Connection connection = dataSource.getConnection()) {
      dbReachable = true;
      schemaReady = schemaPresent(connection);
      if (schemaReady) {
        userCount = users.count();
        ticketCount = tickets.count();
      }
    } catch (Exception ex) {
      dbReachable = false;
    }
    return new SystemStatusResponse(
        dbReachable, schemaReady, isJwtConfigured(), userCount, ticketCount);
  }

  /** True when every schema.sql table exists. */
  private boolean schemaPresent(Connection connection) throws Exception {
    DatabaseMetaData meta = connection.getMetaData();
    List<String> tables = new ArrayList<>();
    try (ResultSet rs = meta.getTables(
        connection.getCatalog(), null, "%", new String[]{"TABLE"})) {
      while (rs.next()) {
        tables.add(rs.getString("TABLE_NAME").toLowerCase());
      }
    }
    return StartupValidator.missingTables(new TreeSet<>(tables), StartupValidator.REQUIRED_TABLES)
        .isEmpty();
  }

  /** Delegates to the validator rule without duplicating it. */
  private boolean isJwtConfigured() {
    return StartupValidator.isSecretValid(jwtSecret);
  }
}
