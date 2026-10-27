package com.technova.campusdesk.ticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.technova.campusdesk.exception.BadRequestException;
import com.technova.campusdesk.exception.BusinessRuleException;
import com.technova.campusdesk.exception.ForbiddenException;
import com.technova.campusdesk.exception.ResourceNotFoundException;
import com.technova.campusdesk.history.entity.StatusHistory;
import com.technova.campusdesk.history.repository.StatusHistoryRepository;
import com.technova.campusdesk.security.AuthUser;
import com.technova.campusdesk.ticket.dto.AssignRequest;
import com.technova.campusdesk.ticket.dto.StatusChangeRequest;
import com.technova.campusdesk.ticket.dto.TicketRequest;
import com.technova.campusdesk.ticket.dto.TicketResponse;
import com.technova.campusdesk.ticket.entity.Ticket;
import com.technova.campusdesk.ticket.entity.TicketCategory;
import com.technova.campusdesk.ticket.entity.TicketPriority;
import com.technova.campusdesk.ticket.entity.TicketStatus;
import com.technova.campusdesk.ticket.repository.TicketRepository;
import com.technova.campusdesk.user.entity.Role;
import com.technova.campusdesk.user.entity.User;
import com.technova.campusdesk.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

/** Business rules of the ticket lifecycle and role permissions (no database needed). */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TicketServiceTest {

  @Mock private TicketRepository tickets;
  @Mock private UserRepository users;
  @Mock private StatusHistoryRepository history;
  @InjectMocks private TicketService service;

  private User admin;
  private User technician;
  private User otherTechnician;
  private User requester;
  private User otherUser;

  @BeforeEach
  void setUp() {
    admin = user(1L, Role.ADMIN);
    technician = user(2L, Role.TECHNICIAN);
    otherTechnician = user(3L, Role.TECHNICIAN);
    requester = user(4L, Role.USER);
    otherUser = user(5L, Role.USER);
    for (User u : new User[]{admin, technician, otherTechnician, requester, otherUser}) {
      when(users.findById(u.getId())).thenReturn(Optional.of(u));
    }
    when(tickets.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
    when(tickets.saveAndFlush(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
  }

  @Test
  void createStartsAbiertaWithoutTechnicianAndRecordsHistory() {
    TicketResponse response = service.create(request(), actor(requester));

    assertThat(response.status()).isEqualTo(TicketStatus.ABIERTA);
    assertThat(response.technician()).isNull();
    StatusHistory row = capturedHistory();
    assertThat(row.getPreviousStatus()).isNull();
    assertThat(row.getNewStatus()).isEqualTo(TicketStatus.ABIERTA);
  }

  @Test
  void onlyUsersCanCreateTickets() {
    assertThatThrownBy(() -> service.create(request(), actor(admin)))
        .isInstanceOf(ForbiddenException.class);
    assertThatThrownBy(() -> service.create(request(), actor(technician)))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  void adminAssignsTechnicianAndTicketMovesToAsignada() {
    Ticket ticket = ticket(10L, TicketStatus.ABIERTA, null);

    TicketResponse response = service.assign(10L, new AssignRequest(technician.getId()), actor(admin));

    assertThat(response.status()).isEqualTo(TicketStatus.ASIGNADA);
    assertThat(ticket.getTechnician()).isSameAs(technician);
    StatusHistory row = capturedHistory();
    assertThat(row.getPreviousStatus()).isEqualTo(TicketStatus.ABIERTA);
    assertThat(row.getNewStatus()).isEqualTo(TicketStatus.ASIGNADA);
    assertThat(row.getChangedBy()).isSameAs(admin);
  }

  @Test
  void adminCanReassignAnAsignadaTicket() {
    Ticket ticket = ticket(10L, TicketStatus.ASIGNADA, technician);

    service.assign(10L, new AssignRequest(otherTechnician.getId()), actor(admin));

    assertThat(ticket.getTechnician()).isSameAs(otherTechnician);
    assertThat(capturedHistory().getNewStatus()).isEqualTo(TicketStatus.ASIGNADA);
  }

  @Test
  void assignRejectsNonTechnicianAndTicketsAlreadyInProgress() {
    ticket(10L, TicketStatus.ABIERTA, null);
    assertThatThrownBy(() -> service.assign(10L, new AssignRequest(otherUser.getId()), actor(admin)))
        .isInstanceOf(BadRequestException.class);

    ticket(11L, TicketStatus.EN_PROCESO, technician);
    assertThatThrownBy(() -> service.assign(11L, new AssignRequest(otherTechnician.getId()), actor(admin)))
        .isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void onlyAdminCanAssign() {
    ticket(10L, TicketStatus.ABIERTA, null);
    assertThatThrownBy(() -> service.assign(10L, new AssignRequest(technician.getId()), actor(requester)))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  void assignedTechnicianFollowsTheLifecycleStepByStep() {
    Ticket ticket = ticket(10L, TicketStatus.ASIGNADA, technician);

    service.changeStatus(10L, new StatusChangeRequest(TicketStatus.EN_PROCESO), actor(technician));
    assertThat(ticket.getStatus()).isEqualTo(TicketStatus.EN_PROCESO);

    service.changeStatus(10L, new StatusChangeRequest(TicketStatus.RESUELTA), actor(technician));
    assertThat(ticket.getStatus()).isEqualTo(TicketStatus.RESUELTA);
  }

  @Test
  void skippingAStatusIsRejected() {
    Ticket ticket = ticket(10L, TicketStatus.ASIGNADA, technician);

    assertThatThrownBy(() ->
        service.changeStatus(10L, new StatusChangeRequest(TicketStatus.RESUELTA), actor(technician)))
        .isInstanceOf(BusinessRuleException.class);
    assertThat(ticket.getStatus()).isEqualTo(TicketStatus.ASIGNADA);
    verify(history, never()).save(any());
  }

  @Test
  void asignadaCannotBeSetThroughStatusEndpoint() {
    ticket(10L, TicketStatus.ABIERTA, null);
    assertThatThrownBy(() ->
        service.changeStatus(10L, new StatusChangeRequest(TicketStatus.ASIGNADA), actor(admin)))
        .isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void technicianCannotTouchTicketsNotAssignedToThem() {
    ticket(10L, TicketStatus.ASIGNADA, technician);
    assertThatThrownBy(() ->
        service.changeStatus(10L, new StatusChangeRequest(TicketStatus.EN_PROCESO), actor(otherTechnician)))
        .isInstanceOf(ForbiddenException.class);
    assertThatThrownBy(() -> service.get(10L, actor(otherTechnician)))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  void requesterClosesOnlyAResueltaTicket() {
    Ticket ticket = ticket(10L, TicketStatus.EN_PROCESO, technician);
    assertThatThrownBy(() ->
        service.changeStatus(10L, new StatusChangeRequest(TicketStatus.CERRADA), actor(requester)))
        .isInstanceOf(BusinessRuleException.class);

    ticket.setStatus(TicketStatus.RESUELTA);
    service.changeStatus(10L, new StatusChangeRequest(TicketStatus.CERRADA), actor(requester));
    assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CERRADA);
    assertThat(capturedHistory().getChangedBy()).isSameAs(requester);
  }

  @Test
  void technicianCannotCloseAndUserCannotResolve() {
    ticket(10L, TicketStatus.RESUELTA, technician);
    assertThatThrownBy(() ->
        service.changeStatus(10L, new StatusChangeRequest(TicketStatus.CERRADA), actor(technician)))
        .isInstanceOf(ForbiddenException.class);

    ticket(11L, TicketStatus.EN_PROCESO, technician);
    assertThatThrownBy(() ->
        service.changeStatus(11L, new StatusChangeRequest(TicketStatus.RESUELTA), actor(requester)))
        .isInstanceOf(ForbiddenException.class);
  }

  @Test
  void userCannotSeeTicketsOfOtherUsers() {
    ticket(10L, TicketStatus.ABIERTA, null);
    assertThatThrownBy(() -> service.get(10L, actor(otherUser)))
        .isInstanceOf(ForbiddenException.class);
    assertThat(service.get(10L, actor(admin)).id()).isEqualTo(10L);
  }

  @Test
  void requesterEditsOnlyWhileAbiertaAndUnassigned() {
    Ticket open = ticket(10L, TicketStatus.ABIERTA, null);
    service.update(10L, request(), actor(requester));
    assertThat(open.getTitle()).isEqualTo("Impresora sin conexion");

    ticket(11L, TicketStatus.ASIGNADA, technician);
    assertThatThrownBy(() -> service.update(11L, request(), actor(requester)))
        .isInstanceOf(BusinessRuleException.class);
  }

  @Test
  void missingTicketAnswersNotFound() {
    when(tickets.findById(99L)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> service.get(99L, actor(admin)))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  // --- helpers ---

  private StatusHistory capturedHistory() {
    ArgumentCaptor<StatusHistory> captor = ArgumentCaptor.forClass(StatusHistory.class);
    verify(history, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
    return captor.getValue();
  }

  private Ticket ticket(Long id, TicketStatus status, User assigned) {
    Ticket ticket = new Ticket();
    ReflectionTestUtils.setField(ticket, "id", id);
    ticket.setTitle("Titulo original");
    ticket.setDescription("Descripcion original del ticket");
    ticket.setCategory(TicketCategory.HARDWARE);
    ticket.setPriority(TicketPriority.MEDIA);
    ticket.setStatus(status);
    ticket.setRequester(requester);
    ticket.setTechnician(assigned);
    when(tickets.findById(id)).thenReturn(Optional.of(ticket));
    return ticket;
  }

  private static TicketRequest request() {
    return new TicketRequest("Impresora sin conexion", "La impresora del piso 2 no responde",
        TicketCategory.HARDWARE, TicketPriority.ALTA);
  }

  private static User user(Long id, Role role) {
    User user = new User("Usuario " + id, "u" + id + "@technova.com", "hash", role);
    ReflectionTestUtils.setField(user, "id", id);
    return user;
  }

  private static AuthUser actor(User user) {
    return new AuthUser(user.getId(), user.getEmail(), user.getFullName(), user.getRole());
  }
}
