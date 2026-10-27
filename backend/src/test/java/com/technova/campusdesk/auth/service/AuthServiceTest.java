package com.technova.campusdesk.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.technova.campusdesk.auth.dto.LoginRequest;
import com.technova.campusdesk.auth.dto.RegisterRequest;
import com.technova.campusdesk.exception.DuplicateResourceException;
import com.technova.campusdesk.security.JwtService;
import com.technova.campusdesk.user.entity.Role;
import com.technova.campusdesk.user.entity.User;
import com.technova.campusdesk.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Registration always creates USER with a BCrypt hash; login never reveals which field failed. */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository users;

  private final PasswordEncoder passwords = new BCryptPasswordEncoder();
  private AuthService service;

  @BeforeEach
  void setUp() {
    JwtService jwt = new JwtService("clave-de-prueba-de-al-menos-32-caracteres!!", 60);
    service = new AuthService(users, passwords, jwt);
  }

  @Test
  void registerAlwaysCreatesUserRoleWithHashedPassword() {
    when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

    service.register(new RegisterRequest("Ana Gomez", " Ana@Technova.com ", "Secreta123"));

    ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
    verify(users).save(saved.capture());
    assertThat(saved.getValue().getRole()).isEqualTo(Role.USER);
    assertThat(saved.getValue().getEmail()).isEqualTo("ana@technova.com");
    assertThat(saved.getValue().getPasswordHash()).isNotEqualTo("Secreta123").startsWith("$2");
    assertThat(passwords.matches("Secreta123", saved.getValue().getPasswordHash())).isTrue();
  }

  @Test
  void registerRejectsDuplicateEmail() {
    when(users.existsByEmail("ana@technova.com")).thenReturn(true);
    assertThatThrownBy(() ->
        service.register(new RegisterRequest("Ana", "ana@technova.com", "Secreta123")))
        .isInstanceOf(DuplicateResourceException.class);
  }

  @Test
  void loginReturnsBearerTokenForValidCredentials() {
    User user = new User("Ana", "ana@technova.com", passwords.encode("Secreta123"), Role.USER);
    when(users.findByEmail("ana@technova.com")).thenReturn(Optional.of(user));

    var response = service.login(new LoginRequest("ana@technova.com", "Secreta123"));

    assertThat(response.tokenType()).isEqualTo("Bearer");
    assertThat(response.token()).isNotBlank();
  }

  @Test
  void loginFailsWithSameErrorForWrongPasswordOrUnknownEmail() {
    User user = new User("Ana", "ana@technova.com", passwords.encode("Secreta123"), Role.USER);
    when(users.findByEmail("ana@technova.com")).thenReturn(Optional.of(user));
    when(users.findByEmail("nadie@technova.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.login(new LoginRequest("ana@technova.com", "otra")))
        .isInstanceOf(BadCredentialsException.class).hasMessage("Credenciales invalidas");
    assertThatThrownBy(() -> service.login(new LoginRequest("nadie@technova.com", "otra")))
        .isInstanceOf(BadCredentialsException.class).hasMessage("Credenciales invalidas");
  }
}
