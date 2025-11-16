// vistaClientes.js - Versión que SOLO usa productos de BD
console.log('✅ vistaClientes.js cargado - Solo BD Real');

// Variables globales
let productosBD = [];
let carrito = JSON.parse(localStorage.getItem('carrito')) || [];
let usuarioActual = null;

// 🔹 INICIALIZACIÓN PRINCIPAL
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 Iniciando vista cliente...');
    inicializarAplicacion();
});

async function inicializarAplicacion() {
    try {
        // 1. Cargar usuario
        await cargarUsuarioActual();

        // 2. Cargar productos SOLO desde BD
        await cargarProductosDesdeBD();

        // 3. Inicializar componentes
        inicializarInterfaz();

        // 4. Inicializar eventos
        inicializarEventos();

        console.log('✅ Aplicación inicializada correctamente');
    } catch (error) {
        console.error('❌ Error inicializando aplicación:', error);
        mostrarError('Error al cargar los productos');
    }
}

// 🔹 CARGAR USUARIO ACTUAL
async function cargarUsuarioActual() {
    try {
        const response = await fetch('/api/usuario-actual', {
            credentials: 'include'
        });

        if (response.ok) {
            const usuario = await response.json();
            usuarioActual = usuario.username;
            console.log('✅ Usuario cargado:', usuarioActual);
            actualizarSaludo(usuarioActual);
        }
    } catch (error) {
        console.warn('⚠️ No se pudo cargar usuario:', error);
        usuarioActual = "Cliente";
    }
}

// 🔹 CARGAR PRODUCTOS DESDE BD - SIN FALLBACK
async function cargarProductosDesdeBD() {
    try {
        console.log('📦 Cargando productos desde BD...');
        const response = await fetch('/productos/api', {
            credentials: 'include'
        });

        if (response.ok) {
            productosBD = await response.json();
            console.log('✅ Productos cargados desde BD:', productosBD.length);

            // Filtrar productos que tienen imágenes
            const productosConImagen = productosBD.filter(producto =>
                producto.imagen && producto.imagen.trim() !== '' && producto.imagen !== 'default.jpg'
            );

            console.log('🖼️ Productos con imágenes:', productosConImagen.length);

            if (productosConImagen.length > 0) {
                renderizarProductosOferta(productosConImagen);
            } else {
                // Si no hay productos con imágenes, mostrar todos pero con placeholders
                console.log('⚠️ No hay productos con imágenes específicas, mostrando todos con placeholders');
                renderizarProductosOferta(productosBD);
            }

        } else {
            throw new Error(`Error HTTP: ${response.status}`);
        }
    } catch (error) {
        console.error('❌ Error crítico cargando productos:', error);
        mostrarError('No se pudieron cargar los productos. Por favor recarga la página.');
    }
}

// 🔹 INICIALIZAR INTERFAZ
function inicializarInterfaz() {
    actualizarUserActions();
    renderizarCategorias();
    actualizarContadorCarrito();
}

// 🔹 ACTUALIZAR ACCIONES DE USUARIO
function actualizarUserActions() {
    const userActions = document.getElementById('userActions');
    if (userActions) {
        userActions.innerHTML = `
            <a href="#" class="user-action-btn">
                <i class='bx bxs-user'></i>
                <span>Mi cuenta</span>
            </a>
            <a href="javascript:void(0)" class="user-action-btn" onclick="abrirModalUbicacion()">
                <i class='bx bxs-map'></i>
                <span>Mi ubicación</span>
            </a>
            <a href="javascript:void(0)" id="carritoBtn" class="user-action-btn">
                <i class='bx bxs-cart'></i>
                <span>Carrito</span>
                <span class="cart-count" id="cartCount">${carrito.reduce((sum, item) => sum + item.cantidad, 0)}</span>
            </a>
            <a href="/logout" class="user-action-btn logout-btn">
                <i class='bx bx-exit'></i>
                <span>Salir</span>
            </a>
        `;
    }
}

// 🔹 ACTUALIZAR SALUDO
function actualizarSaludo(username) {
    const greeting = document.querySelector('.greeting');
    if (greeting) {
        const hora = new Date().getHours();
        let saludo = '';
        if (hora < 12) saludo = '¡Buenos días!';
        else if (hora < 18) saludo = '¡Buenas tardes!';
        else saludo = '¡Buenas noches!';

        greeting.textContent = `${saludo} ${username}`;
    }
}

// 🔹 RENDERIZAR PRODUCTOS EN OFERTA - SOLO BD
function renderizarProductosOferta(productos = productosBD) {
    const carrusel = document.getElementById('carruselProductos');
    if (!carrusel) {
        console.error('❌ No se encontró el carrusel de productos');
        return;
    }

    // Tomar máximo 7 productos
    const productosOferta = productos.slice(0, 7);

    if (productosOferta.length === 0) {
        carrusel.innerHTML = '<div class="cargando-productos">No hay productos disponibles en este momento</div>';
        return;
    }

    console.log('🎨 Renderizando productos reales de BD:', productosOferta.length);

    const productosHTML = productosOferta.map(producto => {
        const imagen = obtenerRutaImagen(producto);
        const tieneImagenReal = !imagen.includes('data:image/svg');

        console.log(`📦 ${producto.nombre} - Imagen: ${tieneImagenReal ? 'REAL' : 'PLACEHOLDER'}`);

        return `
        <div class="card">
            <button class="btn-detalles" onclick="mostrarDetallesProducto('${producto._id}')">
                <i class="fas fa-info-circle"></i>
            </button>
            <img src="${imagen}"
                 alt="${producto.nombre}"
                 class="producto-imagen"
                 onerror="this.src='${crearImagenPlaceholder(producto.nombre)}'">
            <div class="marca">${producto.marca || 'Genérico'}</div>
            <div class="nombre">${producto.nombre}</div>
            <div class="precio-actual">$ ${(producto.precio || 0).toLocaleString()}</div>
            <div class="presentacion">${producto.presentacion || 'Unidad'}</div>
            <button class="btn-comprar" onclick="agregarAlCarrito('${producto._id}')">
                <i class='bx bxs-cart-add'></i> Comprar
            </button>
        </div>
    `}).join('');

    carrusel.innerHTML = productosHTML;
    console.log('✅ Productos reales de BD renderizados:', productosOferta.length);
}

// 🔹 RENDERIZAR CATEGORÍAS
function renderizarCategorias() {
    const categoriesGrid = document.getElementById('categoriesGrid');
    if (!categoriesGrid) return;

    const categorias = [
        { icon: 'fas fa-pills', name: 'Medicamentos', color: '#2ECC71' },
        { icon: 'fas fa-heartbeat', name: 'Cuidado Personal', color: '#3498DB' },
        { icon: 'fas fa-baby', name: 'Maternidad & Bebé', color: '#9B59B6' },
        { icon: 'fas fa-capsules', name: 'Vitaminas', color: '#F39C12' },
        { icon: 'fas fa-first-aid', name: 'Primeros Auxilios', color: '#E74C3C' },
        { icon: 'fas fa-stethoscope', name: 'Equipo Médico', color: '#1ABC9C' }
    ];

    categoriesGrid.innerHTML = categorias.map(cat => `
        <div class="category-card" onclick="filtrarPorCategoria('${cat.name}')">
            <div class="category-icon" style="color: ${cat.color}">
                <i class="${cat.icon}"></i>
            </div>
            <h4>${cat.name}</h4>
            <p>Productos de calidad</p>
        </div>
    `).join('');
}

// 🔹 INICIALIZAR EVENTOS
function inicializarEventos() {
    // Evento de búsqueda
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', function(e) {
            const query = e.target.value.toLowerCase();
            if (query.length > 2) {
                buscarProductos(query);
            }
        });
    }

    // Evento del carrito
    const carritoBtn = document.getElementById('carritoBtn');
    if (carritoBtn) {
        carritoBtn.addEventListener('click', abrirCarrito);
    }

    // Eventos de modales
    inicializarEventosModales();
}

// 🔹 INICIALIZAR EVENTOS DE MODALES
function inicializarEventosModales() {
    // Cerrar modal producto
    const cerrarModal = document.querySelector('.cerrar-modal');
    if (cerrarModal) {
        cerrarModal.addEventListener('click', cerrarModalProducto);
    }

    // Cerrar modal ubicación
    const cerrarUbicacion = document.querySelector('.cerrar-modal-ubicacion');
    if (cerrarUbicacion) {
        cerrarUbicacion.addEventListener('click', cerrarModalUbicacion);
    }

    // Cerrar carrito
    const cerrarCarrito = document.getElementById('cerrarCarrito');
    if (cerrarCarrito) {
        cerrarCarrito.addEventListener('click', cerrarCarritoPanel);
    }

    // Overlay para cerrar modales
    const overlay = document.getElementById('overlay');
    if (overlay) {
        overlay.addEventListener('click', cerrarCarritoPanel);
    }
}

// 🔹 FUNCIONES DE PRODUCTOS - MEJORADAS
function obtenerRutaImagen(producto) {
    // Verificar si el producto tiene imagen válida en BD
    if (producto.imagen && producto.imagen.trim() !== '' && producto.imagen !== 'default.jpg') {
        const imagen = producto.imagen.trim();

        // Si ya es una URL completa o ruta absoluta
        if (imagen.startsWith('http') || imagen.startsWith('/')) {
            console.log(`🖼️ Imagen real encontrada para ${producto.nombre}: ${imagen}`);
            return imagen;
        }

        // Si es un nombre de archivo, construir ruta
        const rutaImagen = `/images/${imagen}`;
        console.log(`🖼️ Construyendo ruta para ${producto.nombre}: ${rutaImagen}`);
        return rutaImagen;
    }

    // Si no tiene imagen válida, usar placeholder
    console.log(`⚠️ ${producto.nombre} no tiene imagen válida, usando placeholder`);
    return crearImagenPlaceholder(producto.nombre);
}

function crearImagenPlaceholder(nombre) {
    const svg = `<svg width="150" height="150" xmlns="http://www.w3.org/2000/svg">
        <rect width="100%" height="100%" fill="#2ECC71"/>
        <text x="50%" y="50%" font-family="Arial" font-size="14" fill="white"
              text-anchor="middle" dy=".3em">${nombre.substring(0, 15)}</text>
    </svg>`;
    return 'data:image/svg+xml;base64,' + btoa(svg);
}

function buscarProductos(query) {
    const resultados = productosBD.filter(producto =>
        producto.nombre.toLowerCase().includes(query) ||
        (producto.categoria && producto.categoria.toLowerCase().includes(query)) ||
        (producto.marca && producto.marca.toLowerCase().includes(query))
    );

    if (resultados.length > 0) {
        mostrarProductosResultados(resultados);
        mostrarNotificacion(`Encontrados ${resultados.length} productos`);
    } else {
        mostrarNotificacion('No se encontraron productos');
    }
}

function mostrarProductosResultados(productos) {
    const carrusel = document.getElementById('carruselProductos');
    if (!carrusel) return;

    const productosHTML = productos.map(producto => `
        <div class="card">
            <img src="${obtenerRutaImagen(producto)}"
                 alt="${producto.nombre}"
                 class="producto-imagen"
                 onerror="this.src='${crearImagenPlaceholder(producto.nombre)}'">
            <div class="marca">${producto.marca || 'Genérico'}</div>
            <div class="nombre">${producto.nombre}</div>
            <div class="precio-actual">$ ${(producto.precio || 0).toLocaleString()}</div>
            <div class="presentacion">${producto.presentacion || 'Unidad'}</div>
            <button class="btn-comprar" onclick="agregarAlCarrito('${producto._id}')">
                <i class='bx bxs-cart-add'></i> Comprar
            </button>
        </div>
    `).join('');

    carrusel.innerHTML = productosHTML;
}

// 🔹 FUNCIONES DEL CARRITO
function agregarAlCarrito(productoId) {
    const producto = productosBD.find(p => p._id === productoId);
    if (producto) {
        const itemExistente = carrito.find(item => item.id === productoId);

        if (itemExistente) {
            itemExistente.cantidad += 1;
        } else {
            carrito.push({
                id: productoId,
                nombre: producto.nombre,
                precio: producto.precio,
                imagen: obtenerRutaImagen(producto),
                cantidad: 1
            });
        }

        localStorage.setItem('carrito', JSON.stringify(carrito));
        actualizarContadorCarrito();
        mostrarNotificacion('✅ Producto agregado al carrito');

        console.log('🛒 Carrito actualizado:', carrito);
    }
}

function actualizarContadorCarrito() {
    const cartCount = document.getElementById('cartCount');
    if (cartCount) {
        const totalItems = carrito.reduce((sum, item) => sum + item.cantidad, 0);
        cartCount.textContent = totalItems;
        cartCount.style.display = totalItems > 0 ? 'inline-block' : 'none';
    }
}

function abrirCarrito() {
    const carritoPanel = document.getElementById('carritoPanel');
    const overlay = document.getElementById('overlay');

    if (carritoPanel && overlay) {
        carritoPanel.classList.add('active');
        overlay.classList.add('active');
        document.body.style.overflow = 'hidden';
        actualizarVistaCarrito();
    }
}

function cerrarCarritoPanel() {
    const carritoPanel = document.getElementById('carritoPanel');
    const overlay = document.getElementById('overlay');

    if (carritoPanel && overlay) {
        carritoPanel.classList.remove('active');
        overlay.classList.remove('active');
        document.body.style.overflow = '';
    }
}

function actualizarVistaCarrito() {
    const carritoItems = document.getElementById('carritoItems');
    const subtotalElement = document.getElementById('subtotal');
    const carritoFooter = document.querySelector('.carrito-footer');

    if (!carritoItems) return;

    if (carrito.length === 0) {
        carritoItems.innerHTML = '<div class="carrito-vacio"><p>Tu carrito está vacío</p></div>';
        if (subtotalElement) subtotalElement.textContent = '$0.00';
        if (carritoFooter) carritoFooter.classList.add('hidden');
        return;
    }

    if (carritoFooter) carritoFooter.classList.remove('hidden');

    let subtotal = 0;
    const itemsHTML = carrito.map(item => {
        const itemSubtotal = item.precio * item.cantidad;
        subtotal += itemSubtotal;

        return `
            <div class="carrito-item">
                <div class="carrito-item-nombre">${item.nombre}</div>
                <div class="carrito-item-precio">$ ${(item.precio || 0).toLocaleString()} x ${item.cantidad}</div>
                <div class="carrito-item-subtotal">$ ${itemSubtotal.toLocaleString()}</div>
                <button class="carrito-item-eliminar" onclick="eliminarDelCarrito('${item.id}')">
                    <i class="fas fa-trash"></i>
                </button>
            </div>
        `;
    }).join('');

    carritoItems.innerHTML = itemsHTML;
    if (subtotalElement) subtotalElement.textContent = `$ ${subtotal.toLocaleString()}`;
}

function eliminarDelCarrito(productoId) {
    carrito = carrito.filter(item => item.id !== productoId);
    localStorage.setItem('carrito', JSON.stringify(carrito));
    actualizarVistaCarrito();
    actualizarContadorCarrito();
    mostrarNotificacion('Producto eliminado del carrito');
}

// 🔹 FUNCIONES DE MODALES
function mostrarDetallesProducto(productoId) {
    const producto = productosBD.find(p => p._id === productoId);
    if (producto) {
        const modal = document.getElementById('modalProducto');
        if (modal) {
            document.getElementById('modalImagen').src = obtenerRutaImagen(producto);
            document.getElementById('modalCategoria').textContent = producto.categoria || 'Medicamento';
            document.getElementById('modalNombre').textContent = producto.nombre;
            document.getElementById('modalDescripcion').textContent = producto.descripcion || 'Producto de calidad garantizada';
            document.getElementById('modalPrecio').textContent = `$ ${(producto.precio || 0).toLocaleString()}`;

            modal.classList.add('active');
            document.body.style.overflow = 'hidden';
        }
    }
}

function cerrarModalProducto() {
    const modal = document.getElementById('modalProducto');
    if (modal) {
        modal.classList.remove('active');
        document.body.style.overflow = '';
    }
}

function abrirModalUbicacion() {
    const modal = document.getElementById('modalUbicacion');
    if (modal) {
        modal.classList.add('active');
        document.body.style.overflow = 'hidden';
        mostrarNotificacion('Modal de ubicación abierto');
    }
}

function cerrarModalUbicacion() {
    const modal = document.getElementById('modalUbicacion');
    if (modal) {
        modal.classList.remove('active');
        document.body.style.overflow = '';
    }
}

function buscarDireccion() {
    mostrarNotificacion('Búsqueda de dirección no implementada');
}

// 🔹 FUNCIONES DE NOTIFICACIÓN
function mostrarNotificacion(mensaje) {
    const notificacion = document.getElementById('notificacion');
    if (notificacion) {
        notificacion.textContent = mensaje;
        notificacion.classList.add('active');

        setTimeout(() => {
            notificacion.classList.remove('active');
        }, 3000);
    }

    console.log('💬 ' + mensaje);
}

function mostrarError(mensaje) {
    const carrusel = document.getElementById('carruselProductos');
    if (carrusel) {
        carrusel.innerHTML = `<div class="error-carga">${mensaje}</div>`;
    }
    console.error('❌ ' + mensaje);
}

// 🔹 FUNCIONES GLOBALES (para HTML)
window.moverCarrusel = function(direction) {
    const carrusel = document.querySelector('.carrusel-items');
    if (carrusel) {
        const scrollAmount = 250;
        carrusel.scrollLeft += direction * scrollAmount;
    }
};

window.filtrarPorCategoria = function(categoria) {
    const resultados = productosBD.filter(p =>
        p.categoria && p.categoria.toLowerCase().includes(categoria.toLowerCase())
    );

    if (resultados.length > 0) {
        mostrarProductosResultados(resultados);
        mostrarNotificacion(`Filtrado por: ${categoria}`);
    } else {
        mostrarNotificacion(`No hay productos en ${categoria}`);
    }
};

// 🔹 INICIALIZACIÓN FINAL
console.log('🎊 vistaClientes.js completamente cargado - SOLO BD REAL');
console.log('📋 Funciones disponibles:');
console.log('   - moverCarrusel(direction)');
console.log('   - agregarAlCarrito(productoId)');
console.log('   - mostrarDetallesProducto(productoId)');
console.log('   - abrirModalUbicacion()');
console.log('   - filtrarPorCategoria(categoria)');