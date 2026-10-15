/* CampusDesk · js/tickets.js — listado, filtros y creación contra la API. */
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

  // Listado (pages/tickets.html)
  if (document.getElementById("ticketsBody")) {
    renderTickets();
    ["filterEstado", "filterPrioridad", "filterCategoria", "searchInput"].forEach((id) =>
      document.getElementById(id)?.addEventListener("input", renderTickets)
    );
    document.getElementById("clearFiltersBtn")?.addEventListener("click", clearFilters);
  }

  // Crear (pages/crear-ticket.html)
  const createForm = document.getElementById("createTicketForm");
  if (createForm) {
    document.getElementById("cancelTicketBtn")?.addEventListener("click", () => history.back());
    createForm.addEventListener("submit", handleCreate);
  }
});

function currentFilters() {
  const v = (id) => document.getElementById(id)?.value || "";
  return {
    status: v("filterEstado"),
    priority: v("filterPrioridad"),
    category: v("filterCategoria"),
    q: v("searchInput").trim().toLowerCase(),
  };
}

async function renderTickets() {
  const ui = window.CampusDeskUI;
  const api = window.CampusDeskApi;
  const body = document.getElementById("ticketsBody");
  const count = document.getElementById("ticketsCount");
  const f = currentFilters();
  try {
    // Estado/prioridad/categoría filtran en el servidor; la búsqueda afina aquí.
    const all = await api.listTickets({ status: f.status, priority: f.priority, category: f.category });
    const rows = all
      .map((t) => ui.normalizeTicket(t))
      .filter((t) =>
        !f.q || `${t.titulo} ${t.id} ${t.solicitante}`.toLowerCase().includes(f.q)
      );
    if (count) count.textContent = `${rows.length} ticket(s)`;
    if (!rows.length) {
      const filtered = f.status || f.priority || f.category || f.q;
      body.innerHTML = `<div class="empty">${
        filtered
          ? "Sin resultados con esos filtros. Usa «Limpiar filtros» para ver el listado completo."
          : "No hay tickets para mostrar."
      }</div>`;
    } else {
      body.innerHTML = rows.map((t) => ui.ticketCard(t)).join("");
    }
    // Limpiar filtros visible solo cuando hay algún filtro activo.
    const hasFilter = !!(f.status || f.priority || f.category || f.q);
    document.getElementById("clearFiltersBtn")?.toggleAttribute("hidden", !hasFilter);
    ui.hideAlert("ticketsAlert");
    ui.preserveRoleInLinks();
  } catch (err) {
    console.debug("[CampusDesk] Error en tickets:", err);
    ui.showAlert("ticketsAlert", err.message || "No se pudo cargar el listado.", "error");
    if (count) count.textContent = "—";
    body.innerHTML = `<div class="empty">Listado no disponible por un error de carga.</div>`;
  }
}

function clearFilters() {
  ["filterEstado", "filterPrioridad", "filterCategoria", "searchInput"].forEach((id) => {
    const n = document.getElementById(id); if (n) n.value = "";
  });
  renderTickets();
}

async function handleCreate(e) {
  e.preventDefault();
  const ui = window.CampusDeskUI;
  const api = window.CampusDeskApi;
  ui.hideAlert("createAlert");
  const titulo = document.getElementById("ctTitulo");
  const desc = document.getElementById("ctDescripcion");
  const categoria = document.getElementById("ctCategoria");
  const prioridad = document.getElementById("ctPrioridad");
  let ok = true;
  const tituloOk = titulo.value.trim().length >= 5;
  titulo.closest(".field").classList.toggle("invalid", !tituloOk);
  titulo.setAttribute("aria-invalid", String(!tituloOk));
  ok = tituloOk && ok;
  const descOk = desc.value.trim().length >= 10;
  desc.closest(".field").classList.toggle("invalid", !descOk);
  desc.setAttribute("aria-invalid", String(!descOk));
  ok = descOk && ok;
  if (!ok) return;

  const btn = document.getElementById("createTicketBtn");
  ui.setLoading(btn, true, "Creando…");
  try {
    // El estado inicial (ABIERTA) lo asigna el backend.
    const created = await api.createTicket({
      title: titulo.value.trim(),
      description: desc.value.trim(),
      category: categoria.value,
      priority: prioridad.value,
    });
    location.href = `ticket-detalle.html?id=${created.id}`;
  } catch (err) {
    console.debug("[CampusDesk] Error al crear:", err);
    ui.showAlert("createAlert", err.message || "No se pudo crear el ticket.", "error");
  } finally {
    ui.setLoading(btn, false);
  }
}
