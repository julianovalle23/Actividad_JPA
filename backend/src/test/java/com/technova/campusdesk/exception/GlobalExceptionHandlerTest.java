package com.technova.campusdesk.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** Every error follows the same JSON shape: timestamp, status, error, message, path. */
class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
  private final MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/tickets/7");

  @Test
  void businessErrorsIncludeStatusAndPath() {
    ResponseEntity<ApiError> response =
        handler.handleNotFound(new ResourceNotFoundException("Ticket no encontrado"), request);

    ApiError body = response.getBody();
    assertThat(response.getStatusCode().value()).isEqualTo(404);
    assertThat(body.status()).isEqualTo(404);
    assertThat(body.error()).isEqualTo("Not Found");
    assertThat(body.message()).isEqualTo("Ticket no encontrado");
    assertThat(body.path()).isEqualTo("/api/tickets/7");
    assertThat(body.timestamp()).isNotNull();
  }

  @Test
  void springMvcErrorsKeepTheirOwnStatusInsteadOf500() {
    assertThat(handler.handleUnexpected(
        new NoResourceFoundException(HttpMethod.GET, "api/nada"), request).getStatusCode().value())
        .isEqualTo(404);
    assertThat(handler.handleUnexpected(
        new HttpRequestMethodNotSupportedException("DELETE"), request).getStatusCode().value())
        .isEqualTo(405);
  }

  @Test
  void unexpectedErrorsHideTheirCause() {
    ResponseEntity<ApiError> response =
        handler.handleUnexpected(new IllegalStateException("password=secret"), request);

    assertThat(response.getStatusCode().value()).isEqualTo(500);
    assertThat(response.getBody().message()).doesNotContain("secret");
  }
}
