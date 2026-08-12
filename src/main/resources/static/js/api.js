// api.js — Utilidades REST + sistema de UI

// ─── Escape HTML (nombre/apellido/usuario vienen de un registro publico sin
// restriccion de caracteres, asi que nunca se interpolan crudos en innerHTML) ─
function escapeHtml(valor) {
    if (valor === null || valor === undefined) return '';
    return String(valor)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

// ─── Autenticación (JWT en localStorage) ────────────────────────────────────
const Auth = {
    getToken()   { return localStorage.getItem('mj_token'); },
    getRol()     { return localStorage.getItem('mj_rol'); },
    getUsuario() { return localStorage.getItem('mj_usuario'); },
    isLoggedIn() { return !!this.getToken(); },
    tieneRol(...roles) { return roles.includes(this.getRol()); },
    guardarSesion(token, rol, usuario) {
        localStorage.setItem('mj_token', token);
        localStorage.setItem('mj_rol', rol);
        localStorage.setItem('mj_usuario', usuario);
    },
    logout() {
        localStorage.removeItem('mj_token');
        localStorage.removeItem('mj_rol');
        localStorage.removeItem('mj_usuario');
        window.location.href = '/login';
    }
};

// ─── API Fetch con manejo de errores robusto ────────────────────────────────
async function apiFetch(url, method = 'GET', body = null) {
    try {
        const headers = { 'Content-Type': 'application/json' };
        const token = Auth.getToken();
        if (token) headers['Authorization'] = 'Bearer ' + token;

        const options = { method, headers };
        if (body) options.body = JSON.stringify(body);

        const res = await fetch(url, options);

        if (res.status === 204) return true;

        // Intentar parsear JSON, con fallback a texto
        let data;
        const contentType = res.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            data = await res.json();
        } else {
            const text = await res.text();
            data = { error: text || 'Error desconocido del servidor.' };
        }

        if (res.status === 401 && token) {
            mostrarAlerta('Tu sesión venció. Iniciá sesión de nuevo.', 'error');
            Auth.logout();
            return null;
        }

        if (!res.ok) {
            const mensaje = data?.error || data?.message || 'Error en la operación.';
            mostrarAlerta(mensaje, 'error');
            return null;
        }
        return data;
    } catch (err) {
        if (err instanceof TypeError && err.message.includes('fetch')) {
            mostrarAlerta('No se puede conectar al servidor. Verificá que la aplicación esté corriendo.', 'error');
        } else {
            mostrarAlerta('Error de conexión: ' + err.message, 'error');
        }
        return null;
    }
}

// ─── Carga de datos con estado visual ───────────────────────────────────────
async function cargarConEstado(fetchFn, tbodyId, colSpan, iconClass, emptyMsg) {
    const tbody = document.getElementById(tbodyId);
    if (!tbody) return null;

    // Estado de carga
    tbody.innerHTML = `<tr><td colspan="${colSpan}" style="padding:0">
        <div class="empty-state"><div class="spinner"></div></div>
    </td></tr>`;

    try {
        const data = await fetchFn();
        if (data === null) {
            // Error ya mostrado por apiFetch, mostrar estado de error en tabla
            tbody.innerHTML = `<tr><td colspan="${colSpan}" style="padding:0">
                <div class="empty-state estado-error">
                    <i class="bi bi-exclamation-circle"></i>
                    <p>No se pudieron cargar los datos.</p>
                    <button class="btn btn-ghost btn-sm" onclick="location.reload()">
                        <i class="bi bi-arrow-clockwise"></i> Reintentar
                    </button>
                </div>
            </td></tr>`;
            return null;
        }
        if (!data || data.length === 0) {
            tbody.innerHTML = `<tr><td colspan="${colSpan}" style="padding:0">
                <div class="empty-state">
                    <i class="bi ${iconClass}"></i>
                    <p>${emptyMsg}</p>
                </div>
            </td></tr>`;
        }
        return data;
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="${colSpan}" style="padding:0">
            <div class="empty-state estado-error">
                <i class="bi bi-wifi-off"></i>
                <p>Error al cargar los datos.<br><span style="font-size:0.75rem">${err.message}</span></p>
                <button class="btn btn-ghost btn-sm" onclick="location.reload()">
                    <i class="bi bi-arrow-clockwise"></i> Reintentar
                </button>
            </div>
        </td></tr>`;
        return null;
    }
}

// ─── Validaciones de formulario ──────────────────────────────────────────────
const Validar = {
    requerido(valor, campo) {
        if (!valor || valor.trim() === '') {
            mostrarAlerta(`El campo "${campo}" es obligatorio.`, 'warning');
            return false;
        }
        return true;
    },
    maxLength(valor, campo, max) {
        if (valor && valor.length > max) {
            mostrarAlerta(`El campo "${campo}" no puede superar ${max} caracteres.`, 'warning');
            return false;
        }
        return true;
    },
    email(valor) {
        if (!valor) return true; // opcional
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!re.test(valor)) {
            mostrarAlerta('El correo no tiene un formato válido.', 'warning');
            return false;
        }
        return true;
    },
    numPositivo(valor, campo) {
        if (valor === '' || valor === null || valor === undefined) return true; // opcional
        const n = parseFloat(valor);
        if (isNaN(n) || n < 0) {
            mostrarAlerta(`El campo "${campo}" debe ser un número positivo.`, 'warning');
            return false;
        }
        return true;
    },
    fechaPasada(valor, campo) {
        if (!valor) return true; // opcional
        const fecha = new Date(valor);
        if (fecha >= new Date()) {
            mostrarAlerta(`"${campo}" debe ser una fecha pasada.`, 'warning');
            return false;
        }
        return true;
    }
};

// ─── Utilidades UI ───────────────────────────────────────────────────────────
function mostrarAlerta(mensaje, tipo = 'success') {
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'toast-container';
        document.body.appendChild(container);
    }
    const icons = { success: 'bi-check-circle', error: 'bi-x-circle', warning: 'bi-exclamation-triangle' };
    const icon = icons[tipo] || 'bi-info-circle';
    const toast = document.createElement('div');
    toast.className = `toast toast-${tipo}`;
    toast.innerHTML = `<i class="bi ${icon} toast-icon"></i><span>${mensaje}</span>`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.transition = 'opacity 0.3s ease';
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 320);
    }, 3800);
}

function modalShow(id) {
    const el = document.getElementById(id);
    if (!el) return;
    el.classList.add('is-active');
    document.body.style.overflow = 'hidden';
}

function modalHide(id) {
    const el = document.getElementById(id);
    if (!el) return;
    el.classList.remove('is-active');
    document.body.style.overflow = '';
}

function formatFecha(fecha) {
    if (!fecha) return '—';
    // Fechas "solo fecha" (ej. "2026-12-31", sin hora) no llevan zona horaria: parsearlas con
    // new Date() las interpreta como medianoche UTC y el navegador las corre un dia para atras
    // en zonas horarias negativas (Argentina, etc). Se arman los componentes a mano en su lugar.
    const soloFecha = /^\d{4}-\d{2}-\d{2}$/.test(fecha);
    if (soloFecha) {
        const [anio, mes, dia] = fecha.split('-').map(Number);
        return new Date(anio, mes - 1, dia).toLocaleDateString('es-AR');
    }
    return new Date(fecha).toLocaleDateString('es-AR');
}

function tipoBadge(tipo) {
    const map = {
        'PlatoFuerte': ['badge-plato',    'Plato Fuerte'],
        'Bebida':      ['badge-bebida',   'Bebida'],
        'Postre':      ['badge-postre',   'Postre'],
        'Adicional':   ['badge-adicional','Adicional'],
        'Alimento':    ['badge-default',  'General']
    };
    const [cls, label] = map[tipo] || ['badge-default', tipo || '—'];
    return `<span class="badge ${cls}">${label}</span>`;
}

function dificultadBadge(d) {
    const map = { 'Fácil': 'badge-success', 'Media': 'badge-warning', 'Difícil': 'badge-error' };
    return `<span class="badge ${map[d] || 'badge-default'}">${d || '—'}</span>`;
}

// ─── Cuenta / visibilidad por rol ────────────────────────────────────────────
const NOMBRES_ROL = { CLIENTE: 'Cliente', CHEF: 'Chef', MESERO: 'Mesero', GERENTE: 'Gerente' };

function renderCuenta() {
    const cont = document.getElementById('navCuenta');
    if (!cont) return;
    if (Auth.isLoggedIn()) {
        cont.innerHTML = `
            <div class="nav-cuenta-info">
                <div class="nav-cuenta-usuario">${escapeHtml(Auth.getUsuario())}</div>
                <div class="nav-cuenta-rol">${escapeHtml(NOMBRES_ROL[Auth.getRol()] || Auth.getRol())}</div>
            </div>
            <a href="#" class="nav-link" onclick="abrirModalCambiarContrasenia(); return false;">
                <i class="bi bi-key"></i><span>Cambiar contraseña</span>
            </a>
            <a href="#" class="nav-link" onclick="Auth.logout(); return false;">
                <i class="bi bi-box-arrow-right"></i><span>Cerrar sesión</span>
            </a>`;
    } else {
        cont.innerHTML = `
            <a href="/login" class="nav-link"><i class="bi bi-box-arrow-in-right"></i><span>Iniciar sesión</span></a>
            <a href="/registro" class="nav-link"><i class="bi bi-person-plus"></i><span>Registrarse</span></a>`;
    }
}

function aplicarVisibilidadPorRol() {
    document.querySelectorAll('[data-rol]').forEach(el => {
        const roles = el.getAttribute('data-rol').split(',');
        el.style.display = Auth.tieneRol(...roles) ? '' : 'none';
    });
}

// ─── Modal "Cambiar contraseña" ──────────────────────────────────────────────
// Se inyecta una sola vez en el DOM (lazy, como el toastContainer de mostrarAlerta)
// para no tener que duplicar el markup del modal en las 14 paginas.
function abrirModalCambiarContrasenia() {
    if (!document.getElementById('modalCambiarContraseniaOverlay')) {
        const overlay = document.createElement('div');
        overlay.className = 'modal-overlay';
        overlay.id = 'modalCambiarContraseniaOverlay';
        overlay.innerHTML = `
            <div class="modal modal-sm">
                <div class="modal-header">
                    <h3 class="modal-title">Cambiar contraseña</h3>
                    <button class="modal-close" onclick="modalHide('modalCambiarContraseniaOverlay')"><i class="bi bi-x-lg"></i></button>
                </div>
                <div class="modal-body">
                    <div class="form-group">
                        <label class="form-label">Contraseña actual <span class="required">*</span></label>
                        <input type="password" class="form-input" id="ccActual" autocomplete="current-password">
                    </div>
                    <div class="form-group">
                        <label class="form-label">Contraseña nueva <span class="required">*</span></label>
                        <input type="password" class="form-input" id="ccNueva" placeholder="Mínimo 8 caracteres" autocomplete="new-password">
                    </div>
                    <div class="form-group">
                        <label class="form-label">Repetir contraseña nueva <span class="required">*</span></label>
                        <input type="password" class="form-input" id="ccRepetir" autocomplete="new-password">
                    </div>
                </div>
                <div class="modal-footer">
                    <button class="btn btn-ghost" onclick="modalHide('modalCambiarContraseniaOverlay')">Cancelar</button>
                    <button class="btn btn-primary" onclick="enviarCambioContrasenia()">
                        <i class="bi bi-check-lg"></i> Guardar
                    </button>
                </div>
            </div>`;
        document.body.appendChild(overlay);
        overlay.addEventListener('click', e => { if (e.target === overlay) modalHide(overlay.id); });
    }
    document.getElementById('ccActual').value = '';
    document.getElementById('ccNueva').value = '';
    document.getElementById('ccRepetir').value = '';
    modalShow('modalCambiarContraseniaOverlay');
}

async function enviarCambioContrasenia() {
    const actual  = document.getElementById('ccActual').value;
    const nueva   = document.getElementById('ccNueva').value;
    const repetir = document.getElementById('ccRepetir').value;

    if (!Validar.requerido(actual, 'Contraseña actual')) return;
    if (nueva.length < 8) { mostrarAlerta('La contraseña nueva debe tener al menos 8 caracteres.', 'warning'); return; }
    if (nueva !== repetir) { mostrarAlerta('Las contraseñas nuevas no coinciden.', 'warning'); return; }

    const resultado = await apiFetch('/api/v1/auth/contrasenia', 'PUT', {
        contraseniaActual: actual, contraseniaNueva: nueva
    });
    if (resultado) {
        mostrarAlerta('Contraseña actualizada correctamente.', 'success');
        modalHide('modalCambiarContraseniaOverlay');
    }
}

// ─── Event listeners globales ────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    renderCuenta();
    aplicarVisibilidadPorRol();
    // Cerrar modal al click fuera
    document.querySelectorAll('.modal-overlay').forEach(overlay => {
        overlay.addEventListener('click', e => {
            if (e.target === overlay) modalHide(overlay.id);
        });
    });
    // Cerrar con Escape
    document.addEventListener('keydown', e => {
        if (e.key === 'Escape') {
            document.querySelectorAll('.modal-overlay.is-active').forEach(m => modalHide(m.id));
        }
    });

    // ── Sidebar móvil: toggle ──
    const toggleBtn = document.getElementById('sidebarToggle');
    const sidebar   = document.querySelector('.nav-sidebar');
    const overlay   = document.getElementById('sidebarOverlay');

    if (toggleBtn && sidebar) {
        toggleBtn.addEventListener('click', () => {
            sidebar.classList.toggle('is-open');
            if (overlay) overlay.classList.toggle('is-active');
        });
    }
    if (overlay) {
        overlay.addEventListener('click', () => {
            sidebar.classList.remove('is-open');
            overlay.classList.remove('is-active');
        });
    }
});
