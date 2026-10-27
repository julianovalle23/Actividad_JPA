/* CampusDesk · js/admin.js — panel administrativo contra la API (solo ADMIN). */
"use strict";

document.addEventListener("DOMContentLoaded", () => {
  const ui = window.CampusDeskUI;
  ui.initSidebar();
  ui.initLogout();
  ui.applyRoleVisibility();
  if (!window.CampusDeskApi?.getToken()) {
    location.href = "login.html";
    return;
  }
  // Solo ADMIN usa este panel (el backend responde 403 de todas formas).
  if (window.CampusDeskApi.getSessionUser()?.role !== "ADMIN") {
    location.href = "dashboard.html";
    return;
  }
  loadAdmin();
  document.querySelectorAll(".tab-btn").forEach((btn) => {
    btn.addEventListener("click", () => switchTab(btn.dataset.tab));
    btn.addEventListener("keydown", onTabKey);
  });
  document.getElementById("assignForm")?.addEventListener("submit", handleAssign);
  document.getElementById("usersBody")?.addEventListener("click", handleUserAction);
});

async function loadAdmin() {
  const ui = window.CampusDeskUI;
  const api = window.CampusDeskApi;
  try {
    const [tickets, techs, users, summary] = await Promise.all([
      api.listTickets(),
      api.listTechnicians(),
      api.listUsers(),
      api.getSummary(),
    ]);
    renderCounts(tickets, techs, users, summary);
    renderAdminTickets(tickets);
    renderTechs(techs);
    renderUsers(users);
    fillAssignSelects(tickets, techs);
    ui.hideAlert("adminAlert");
    ui.preserveRoleInLinks();
  } catch (err) {
    console.debug("[CampusDesk] Error en admin:", err);
    renderAdminLoadError(err.message || "No se pudieron cargar los datos de administración.");
  }
}

function renderCounts(tickets, techs, users, summary) {
  const set = (id, v) => { const n = document.getElementById(id); if (n) n.textContent = v; };
  set("countTickets", summary?.total ?? tickets.length);
  set("countTechs", techs.length);
  set("countUsers", users.length);
  const stats = document.getElementById("adminStatsText");
  if (stats && summary) {
    stats.textContent = `ABIERTA: ${summary.abiertas} · ASIGNADA: ${summary.asignadas} · `
      + `EN_PROCESO: ${summary.enProceso} · RESUELTA: ${summary.resueltas} · CERRADA: ${summary.cerradas}.`;
  }
}

function renderAdminTickets(tickets) {
  const ui = window.CampusDeskUI;
  const tb = document.getElementById("adminTicketsBody");
  if (tb) tb.innerHTML = tickets.length
    ? tickets.map((t) => ui.ticketCard(ui.normalizeTicket(t),
      { showDate: false, showAuthor: false, showCategory: false, compact: true })).join("")
    : `<div class="empty">No hay tickets para mostrar.</div>`;
}

function renderTechs(techs) {
  const ui = window.CampusDeskUI;
  const tech = document.getElementById("techBody");
  if (tech) tech.innerHTML = techs.length ? techs.map((x) => `
    <tr><td class="cell-title">${ui.escapeHtml(x.fullName)}</td>
    <td>${ui.escapeHtml(x.email)}</td><td><span class="badge cat">${ui.escapeHtml(x.role)}</span></td></tr>`).join("")
    : `<tr><td colspan="3"><div class="empty">No hay técnicos registrados.</div></td></tr>`;
}

function renderUsers(users) {
  const ui = window.CampusDeskUI;
  const body = document.getElementById("usersBody");
  if (body) body.innerHTML = users.length ? users.map((u) => `
    <tr><td class="cell-title">${ui.escapeHtml(u.fullName)}</td>
    <td>${ui.escapeHtml(u.email)}</td><td><span class="badge cat">${ui.escapeHtml(u.role)}</span></td>
    <td>${u.role === "USER" ? `<button class="btn btn-secondary btn-sm" type="button" data-promote-user="${u.id}">Promover a técnico</button>` : "—"}</td></tr>`).join("")
    : `<tr><td colspan="4"><div class="empty">No hay usuarios registrados.</div></td></tr>`;
}

async function handleUserAction(e) {
  const button = e.target.closest("[data-promote-user]");
  if (!button) return;
  const userId = button.dataset.promoteUser;
  const ui = window.CampusDeskUI;
  if (!window.confirm("¿Promover esta cuenta al rol TECHNICIAN? Podrá ver y gestionar los tickets que se le asignen.")) return;
  button.disabled = true;
  try {
    await window.CampusDeskApi.promoteUserToTechnician(userId);
    ui.showAlert("usersAlert", "Cuenta promovida a técnico correctamente.", "success");
    await loadAdmin();
  } catch (err) {
    console.debug("[CampusDesk] Error al promover usuario:", err);
    ui.showAlert("usersAlert", err.message || "No se pudo promover la cuenta.", "error");
    button.disabled = false;
  }
}

function fillAssignSelects(tickets, techs) {
  const ui = window.CampusDeskUI;
  const selT = document.getElementById("assignTicket");
  const selTech = document.getElementById("assignTech");
  if (selT) selT.innerHTML = tickets
    .map((t) => `<option value="${t.id}">#${t.id} — ${ui.escapeHtml(t.title)}</option>`).join("");
  if (selTech) selTech.innerHTML = techs
    .map((x) => `<option value="${x.id}">${ui.escapeHtml(x.fullName)}</option>`).join("");
}

function switchTab(name, focusTab = false) {
  const tabs = [...document.querySelectorAll(".tab-btn")];
  tabs.forEach((b) => {
    const selected = b.dataset.tab === name;
    b.classList.toggle("active", selected);
    b.setAttribute("aria-selected", String(selected));
    b.tabIndex = selected ? 0 : -1;
    if (selected && focusTab) b.focus();
  });
  document.querySelectorAll(".tab-panel").forEach((p) => {
    const selected = p.id === `tab-${name}`;
    p.classList.toggle("active", selected);
    if (selected) p.removeAttribute("hidden");
    else p.setAttribute("hidden", "");
  });
}

// Teclado: flechas ←/→ (e Inicio/Fin) entre pestañas, patrón tabs ARIA.
function onTabKey(e) {
  if (!["ArrowLeft", "ArrowRight", "Home", "End"].includes(e.key)) return;
  e.preventDefault();
  const tabs = [...document.querySelectorAll(".tab-btn")];
  let i = tabs.indexOf(e.currentTarget);
  if (e.key === "ArrowRight") i = (i + 1) % tabs.length;
  else if (e.key === "ArrowLeft") i = (i - 1 + tabs.length) % tabs.length;
  else if (e.key === "Home") i = 0;
  else i = tabs.length - 1;
  switchTab(tabs[i].dataset.tab, true);
}

async function handleAssign(e) {
  e.preventDefault();
  const ui = window.CampusDeskUI;
  const api = window.CampusDeskApi;
  const btn = document.getElementById("assignBtn");
  ui.setLoading(btn, true, "Asignando…");
  try {
    const ticketId = document.getElementById("assignTicket").value;
    const techId = document.getElementById("assignTech").value;
    await api.assignTechnician(ticketId, techId);
    ui.showAlert("assignAlert", "Técnico asignado correctamente.", "success");
    loadAdmin();
  } catch (err) {
    console.debug("[CampusDesk] Error en asignación:", err);
    ui.showAlert("assignAlert", err.message || "No se pudo validar la asignación.", "error");
  } finally {
    ui.setLoading(btn, false);
  }
}

// Error de carga visible sobre las pestañas (da igual cuál esté activa)
// + filas de error en cada tabla para no mostrar datos incompletos.
function renderAdminLoadError(message) {
  const ui = window.CampusDeskUI;
  ui.showAlert("adminAlert", message, "error");
  const errorRow = (cols, text) => `<tr><td colspan="${cols}"><div class="empty">${text}</div></td></tr>`;
  const tb = document.getElementById("adminTicketsBody");
  if (tb) tb.innerHTML = `<div class="empty">Tickets no disponibles por un error de carga.</div>`;
  const tech = document.getElementById("techBody");
  if (tech) tech.innerHTML = errorRow(3, "Técnicos no disponibles por un error de carga.");
  const usersBody = document.getElementById("usersBody");
  if (usersBody) usersBody.innerHTML = errorRow(4,"Usuarios no disponibles por un error de carga.");
}
