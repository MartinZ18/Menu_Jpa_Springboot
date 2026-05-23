// api.js — Utilidades REST + sistema de UI

// ─── API Fetch con manejo de errores robusto ────────────────────────────────
async function apiFetch(url, method = 'GET', body = null) {
    try {
        const options = { method, headers: { 'Content-Type': 'application/json' } };
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

// ─── Event listeners globales ────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
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
