/* CampusDesk · js/ticket-detalle.js — detalle, comentarios e historial desde la API. */
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
  renderDetail();
  document.getElementById("commentForm")?.addEventListener("submit", handleComment);
  document.getElementById("editForm")?.addEventListener("submit", handleEditSubmit);
  document.getElementById("cancelEditBtn")?.addEventListener("click", () => {
    document.getElementById("editForm").style.display = "none";
  });
});

function ticketIdFromUrl() {
  return new URLSearchParams(location.search).get("id");
}

async function renderDetail() {
  const ui = window.CampusDeskUI;
  const api = window.CampusDeskApi;
  const content = ui.el("detalleContent");
  const notFound = ui.el("detalleNotFound");
  const showNotFound = () => {
    if (content) content.style.display = "none";
    if (notFound) notFound.style.display = "";
    ui.el("dTitulo").textContent = "Detalle del ticket";
    ui.el("dEstado").innerHTML = "";
  };
  const id = ticketIdFromUrl();
  if (!id) { showNotFound(); return; }
  try {
    const [raw, comments, history] = await Promise.all([
      api.getTicket(id),
      api.getComments(id),
      api.getHistory(id),
    ]);
    const t = ui.normalizeTicket(raw);
    ui.el("dId").textContent = `#${t.id}`;
    ui.el("dTitulo").textContent = t.titulo;
    ui.el("dDescripcion").textContent = t.descripcion;
    ui.el("dCategoria").innerHTML = ui.categoryBadge(t.categoria);
    ui.el("dPrioridad").innerHTML = ui.priorityBadge(t.prioridad);
    ui.el("dEstado").innerHTML = ui.statusBadge(t.estado);
    ui.el("dSolicitante").textContent = t.solicitante;
    ui.el("dTecnico").textContent = t.tecnico;
    ui.el("dFecha").textContent = ui.formatDate(t.fecha);

    const closed = t.estado === "CERRADA";
    ui.el("readonlyNote").style.display = closed ? "" : "none";
    const commentBox = ui.el("commentText");
    const commentBtn = ui.el("commentBtn");
    if (closed) { commentBox.disabled = true; commentBtn.disabled = true; }

    renderComments(comments.map((c) => ui.normalizeComment(c)));
    renderHistory(history);
    renderActions(raw);
  } catch (err) {
    console.debug("[CampusDesk] Error en detalle:", err);
    showNotFound();
  }
}

// Acciones permitidas según rol y estado (el backend valida de nuevo).
function renderActions(raw) {
  const ui = window.CampusDeskUI;
  const me = window.CampusDeskApi?.getSessionUser();
  const card = ui.el("actionsCard");
  const box = ui.el("statusActions");
  const form = ui.el("editForm");
  if (!card || !box || !me) return;
  ui.hideAlert("actionsAlert");
  box.innerHTML = "";
  form.style.display = "none";

  const isOwner = raw.requester?.id === me.id;
  const isTech = raw.technician?.id === me.id;
  const canEdit = me.role === "USER" && isOwner && raw.status === "ABIERTA" && !raw.technician;
  const nextByTech = me.role === "TECHNICIAN" && isTech
    ? { ASIGNADA: "EN_PROCESO", EN_PROCESO: "RESUELTA" }[raw.status] : null;
  const canClose = me.role === "USER" && isOwner && raw.status === "RESUELTA";

  const actions = [];
  if (canEdit) actions.push({ id: "edit", label: "Editar ticket", kind: "secondary" });
  if (nextByTech) actions.push({ id: "status", label: `Marcar ${nextByTech.replace("_", " ")}`, kind: "primary", status: nextByTech });
  if (canClose) actions.push({ id: "status", label: "Cerrar ticket", kind: "primary", status: "CERRADA" });
  if (!actions.length) { card.style.display = "none"; return; }

  card.style.display = "";
  actions.forEach((a) => {
    const btn = document.createElement("button");
    btn.type = "button";
    btn.className = `btn btn-${a.kind} btn-sm`;
    btn.textContent = a.label;
    btn.addEventListener("click", () => {
      if (a.id === "edit") openEditForm(raw);
      else changeTicketStatus(raw.id, a.status, btn);
    });
    box.appendChild(btn);
  });
}

// Muestra el formulario de edición con los datos actuales.
function openEditForm(raw) {
  const ui = window.CampusDeskUI;
  ui.el("editTitulo").value = raw.title || "";
  ui.el("editDescripcion").value = raw.description || "";
  ui.el("editCategoria").value = raw.category || "OTROS";
  ui.el("editPrioridad").value = raw.priority || "MEDIA";
  ui.el("editForm").style.display = "";
  ui.el("editTitulo").focus();
}

async function handleEditSubmit(e) {
  e.preventDefault();
  const ui = window.CampusDeskUI;
  const api = window.CampusDeskApi;
  ui.hideAlert("actionsAlert");
  const titulo = ui.el("editTitulo");
  const desc = ui.el("editDescripcion");
  const tituloOk = titulo.value.trim().length >= 5;
  const descOk = desc.value.trim().length >= 10;
  titulo.closest(".field").classList.toggle("invalid", !tituloOk);
  titulo.setAttribute("aria-invalid", String(!tituloOk));
  desc.closest(".field").classList.toggle("invalid", !descOk);
  desc.setAttribute("aria-invalid", String(!descOk));
  if (!tituloOk || !descOk) return;

  const btn = ui.el("saveEditBtn");
  ui.setLoading(btn, true, "Guardando…");
  try {
    await api.updateTicket(ticketIdFromUrl(), {
      title: titulo.value.trim(),
      description: desc.value.trim(),
      category: ui.el("editCategoria").value,
      priority: ui.el("editPrioridad").value,
    });
    ui.showAlert("actionsAlert", "Ticket actualizado correctamente.", "success");
    renderDetail();
  } catch (err) {
    console.debug("[CampusDesk] Error al editar:", err);
    ui.showAlert("actionsAlert", err.message || "No se pudo guardar.", "error");
  } finally {
    ui.setLoading(btn, false);
  }
}

async function changeTicketStatus(id, status, btn) {
  const ui = window.CampusDeskUI;
  const api = window.CampusDeskApi;
  ui.setLoading(btn, true, "Aplicando…");
  try {
    await api.changeStatus(id, status);
    ui.showAlert("actionsAlert", `Ticket marcado como ${status.replace("_", " ")}.`, "success");
    renderDetail();
  } catch (err) {
    console.debug("[CampusDesk] Error de estado:", err);
    ui.showAlert("actionsAlert", err.message || "No se pudo cambiar el estado.", "error");
  } finally {
    ui.setLoading(btn, false);
  }
}

function renderComments(comments) {
  const ui = window.CampusDeskUI;
  const list = ui.el("commentsList");
  list.innerHTML = comments.length ? comments.map((c) => `
    <div class="comment">
      <div class="comment-head"><b>${ui.escapeHtml(c.autor)}</b><span>${ui.formatDateTime(c.fecha)}</span></div>
      <div>${ui.escapeHtml(c.contenido)}</div>
    </div>`).join("")
    : `<div class="empty">Este ticket aún no tiene comentarios.</div>`;
}

// Historial real del backend en orden cronológico (solo lectura):
// cada cambio con estado anterior → nuevo, quién lo hizo y cuándo.
// Incluye las reasignaciones (ASIGNADA → ASIGNADA).
function renderHistory(events) {
  const ui = window.CampusDeskUI;
  const list = ui.el("historyList");
  if (!events.length) {
    list.innerHTML = `<li><span class="t-dot" aria-hidden="true"></span><div class="cell-sub">Sin cambios registrados.</div></li>`;
    return;
  }
  list.innerHTML = events.map((e, i) => {
    const isLast = i === events.length - 1;
    const from = e.previousStatus ? `${ui.statusBadge(e.previousStatus)} → ` : "";
    const label = e.previousStatus === null ? "Creado"
      : e.previousStatus === e.newStatus ? "Reasignado" : "Cambio de estado";
    return `<li class="${isLast ? "current" : "done"}"><span class="t-dot" aria-hidden="true"></span>
      ${from}${ui.statusBadge(e.newStatus)}
      <div class="cell-sub">${label} por ${ui.escapeHtml(e.changedBy?.fullName || "—")} · ${ui.formatDateTime(e.changedAt)}</div></li>`;
  }).join("");
}

async function handleComment(e) {
  e.preventDefault();
  const ui = window.CampusDeskUI;
  const api = window.CampusDeskApi;
  const box = ui.el("commentText");
  if (!box.value.trim()) {
    box.setAttribute("aria-invalid", "true");
    ui.showAlert("commentAlert", "Escribe un comentario antes de enviar.", "error");
    return;
  }
  box.setAttribute("aria-invalid", "false");
  const btn = ui.el("commentBtn");
  ui.setLoading(btn, true, "Enviando…");
  try {
    await api.addComment(ticketIdFromUrl(), box.value.trim());
    box.value = "";
    ui.hideAlert("commentAlert");
    renderComments((await api.getComments(ticketIdFromUrl())).map((c) => ui.normalizeComment(c)));  } catch (err) {
    console.debug("[CampusDesk] Error en comentario:", err);
    ui.showAlert("commentAlert", err.message || "No se pudo enviar el comentario.", "error");
  } finally {
    ui.setLoading(btn, false);
  }
}
