package com.App.Lfarma.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.App.Lfarma.entity.Producto;
import com.App.Lfarma.repository.ProductoRepository;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProductoService {

    private static final Logger log = LoggerFactory.getLogger(ProductoService.class);

    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> listarProductos() {
        try {
            log.info("🔍 SERVICE - Obteniendo TODOS los productos sin paginación");
            List<Producto> productos = productoRepository.findAll();

            log.info("✅ SERVICE - Total productos encontrados en BD: {}", productos.size());

            if (!productos.isEmpty()) {
                log.info("📋 SERVICE - Primeros 5 productos:");
                for (int i = 0; i < Math.min(5, productos.size()); i++) {
                    Producto p = productos.get(i);
                    log.info("   {}. {} (ID: {}, Código: {})",
                            i + 1, p.getNombre(), p.getId(), p.getCodigo());
                }
            } else {
                log.warn("⚠️ SERVICE - No se encontraron productos en la base de datos");
            }

            return productos;
        } catch (Exception e) {
            log.error("❌ ERROR SERVICE - Error al listar productos: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener la lista de productos: " + e.getMessage());
        }
    }

    public List<Producto> listarProductosOrdenados(Sort sort) {
        try {
            List<Producto> productos = productoRepository.findAll(sort);
            log.debug("📦 Listando {} productos ordenados", productos.size());
            return productos;
        } catch (Exception e) {
            log.error("❌ Error al listar productos ordenados: {}", e.getMessage());
            throw new RuntimeException("Error al obtener productos ordenados: " + e.getMessage());
        }
    }

    // ✅✅✅ CORRECCIÓN CRÍTICA: Paginación con categoría
    public Page<Producto> listarProductosPaginadas(String categoria, Pageable pageable) {
        try {
            log.info("🔍 SERVICE - Buscando productos paginados con categoría: '{}'", categoria);
            log.info("🔍 SERVICE - Pageable: página {}, tamaño {}", pageable.getPageNumber(), pageable.getPageSize());

            Page<Producto> productosPage;

            if (categoria != null && !categoria.trim().isEmpty() && !categoria.equals("todos")) {
                log.info("📂 SERVICE - Filtrando por categoría específica: '{}'", categoria);
                productosPage = productoRepository.findByCategoria(categoria, pageable);
            } else {
                log.info("📋 SERVICE - Listando TODOS los productos (sin filtro de categoría)");
                productosPage = productoRepository.findAll(pageable);
            }

            // ✅✅✅ DEBUG CRÍTICO: Verificar qué devuelve el repository
            log.info("✅ SERVICE - Resultados obtenidos del REPOSITORY:");
            log.info("   - Total elementos en BD: {}", productosPage.getTotalElements());
            log.info("   - Elementos en esta página: {}", productosPage.getNumberOfElements());
            log.info("   - Contenido size: {}", productosPage.getContent().size());
            log.info("   - Total páginas: {}", productosPage.getTotalPages());

            // Verificar los productos individualmente
            List<Producto> contenido = productosPage.getContent();
            if (!contenido.isEmpty()) {
                log.info("🔍 SERVICE - Productos en contenido ({} elementos):", contenido.size());
                for (int i = 0; i < contenido.size(); i++) {
                    Producto p = contenido.get(i);
                    log.info("   {}. {} (ID: {}, Código: {}, Categoría: {})",
                            i + 1, p.getNombre(), p.getId(), p.getCodigo(), p.getCategoria());
                }
            } else {
                log.warn("⚠️ SERVICE - No se encontraron productos en la página actual");

                // Verificar si hay productos en la BD pero no en esta página
                if (productosPage.getTotalElements() > 0) {
                    log.info("ℹ️ SERVICE - Hay {} productos en BD pero 0 en esta página.",
                            productosPage.getTotalElements());
                }
            }

            return productosPage;

        } catch (Exception e) {
            log.error("❌ ERROR SERVICE - Error en listarProductosPaginadas con categoría '{}': {}",
                    categoria, e.getMessage(), e);
            throw new RuntimeException("Error al obtener productos paginados: " + e.getMessage());
        }
    }

    // ✅✅✅ CORRECCIÓN CRÍTICA: Paginación simple sin filtros
    public Page<Producto> listarProductosPaginadas(Pageable pageable) {
        try {
            log.info("🔍 SERVICE - Listando todos los productos paginados (sin filtros)");
            log.info("🔍 SERVICE - Pageable: página {}, tamaño {}", pageable.getPageNumber(), pageable.getPageSize());

            // ✅ PRIMERO: Verificar cuántos productos hay en total
            long totalProductos = productoRepository.count();
            log.info("📊 SERVICE - Total productos en BD (count): {}", totalProductos);

            Page<Producto> productosPage = productoRepository.findAll(pageable);

            // ✅✅✅ DEBUG CRÍTICO
            log.info("✅ SERVICE - Resultados obtenidos del REPOSITORY:");
            log.info("   - Total elementos: {}", productosPage.getTotalElements());
            log.info("   - Elementos en esta página: {}", productosPage.getNumberOfElements());
            log.info("   - Contenido size: {}", productosPage.getContent().size());
            log.info("   - Total páginas: {}", productosPage.getTotalPages());

            List<Producto> contenido = productosPage.getContent();
            if (!contenido.isEmpty()) {
                log.info("🔍 SERVICE - Productos en contenido ({} elementos):", contenido.size());
                for (int i = 0; i < contenido.size(); i++) {
                    Producto p = contenido.get(i);
                    log.info("   {}. {} (ID: {}, Código: {})",
                            i + 1, p.getNombre(), p.getId(), p.getCodigo());
                }
            } else {
                log.warn("⚠️ SERVICE - No se encontraron productos en la página actual");

                // Información adicional para diagnóstico
                if (productosPage.getTotalElements() > 0) {
                    log.info("ℹ️ SERVICE - CONTRADICCIÓN: Hay {} productos en BD pero 0 en esta página.",
                            productosPage.getTotalElements());
                    log.info("ℹ️ SERVICE - Página solicitada: {}", pageable.getPageNumber());
                }
            }

            return productosPage;
        } catch (Exception e) {
            log.error("❌ ERROR SERVICE - Error en listarProductosPaginadas: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener productos paginados: " + e.getMessage());
        }
    }

    // ✅ MÉTODO ALTERNATIVO: Para mantener compatibilidad con código existente
    public Page<Producto> listarProductosPaginados(String categoria, Pageable pageable) {
        return listarProductosPaginadas(categoria, pageable);
    }

    // ✅✅✅ CORRECCIÓN: Búsqueda de productos con manejo robusto de parámetros
    public Page<Producto> buscarProductos(String searchTerm, String categoria, Pageable pageable) {
        try {
            // Validar y limpiar parámetros
            String terminoBusqueda = (searchTerm != null) ? searchTerm.trim() : "";
            String categoriaFiltro = (categoria != null) ? categoria.trim() : "";

            log.info("🔍 SERVICE - Iniciando búsqueda:");
            log.info("   - Término: '{}'", terminoBusqueda);
            log.info("   - Categoría: '{}'", categoriaFiltro);
            log.info("   - Pageable: página {}, tamaño {}", pageable.getPageNumber(), pageable.getPageSize());

            Page<Producto> resultados;

            if (!terminoBusqueda.isEmpty() && !categoriaFiltro.isEmpty()) {
                // Búsqueda por término y categoría
                log.info("🔍 SERVICE - Búsqueda COMBINADA: '{}' en categoría '{}'", terminoBusqueda, categoriaFiltro);
                resultados = productoRepository.findByNombreContainingIgnoreCaseAndCategoria(terminoBusqueda, categoriaFiltro, pageable);
            } else if (!terminoBusqueda.isEmpty()) {
                // Búsqueda solo por término
                log.info("🔍 SERVICE - Búsqueda por TÉRMINO: '{}'", terminoBusqueda);
                resultados = productoRepository.findByNombreContainingIgnoreCase(terminoBusqueda, pageable);
            } else if (!categoriaFiltro.isEmpty()) {
                // Búsqueda solo por categoría
                log.info("📂 SERVICE - Búsqueda por CATEGORÍA: '{}'", categoriaFiltro);
                resultados = productoRepository.findByCategoria(categoriaFiltro, pageable);
            } else {
                // Sin filtros, listar todo
                log.info("📋 SERVICE - Sin filtros, listando TODOS los productos");
                resultados = productoRepository.findAll(pageable);
            }

            // ✅✅✅ DEBUG CRÍTICO
            log.info("✅ SERVICE - Búsqueda completada:");
            log.info("   - Resultados en página: {}", resultados.getNumberOfElements());
            log.info("   - Total elementos encontrados: {}", resultados.getTotalElements());
            log.info("   - Contenido size: {}", resultados.getContent().size());
            log.info("   - Total páginas: {}", resultados.getTotalPages());

            List<Producto> contenido = resultados.getContent();
            if (!contenido.isEmpty()) {
                log.info("🔍 SERVICE - Productos encontrados:");
                for (int i = 0; i < contenido.size(); i++) {
                    Producto p = contenido.get(i);
                    log.info("   {}. {} (ID: {}, Código: {})",
                            i + 1, p.getNombre(), p.getId(), p.getCodigo());
                }
            }

            return resultados;

        } catch (Exception e) {
            log.error("❌ ERROR SERVICE - Error en buscarProductos - Término: '{}', Categoría: '{}': {}",
                    searchTerm, categoria, e.getMessage(), e);
            throw new RuntimeException("Error en la búsqueda de productos: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Búsqueda por código con validación
    public Optional<Producto> buscarPorCodigo(String codigo) {
        try {
            if (codigo == null || codigo.trim().isEmpty()) {
                log.warn("⚠️ Código de producto vacío o nulo");
                return Optional.empty();
            }

            String codigoLimpio = codigo.trim();
            log.info("🔍 SERVICE - Buscando producto por código: '{}'", codigoLimpio);

            Optional<Producto> producto = productoRepository.findByCodigo(codigoLimpio);

            if (producto.isPresent()) {
                log.info("✅ SERVICE - Producto encontrado por código: {} - {}",
                        codigoLimpio, producto.get().getNombre());
            } else {
                log.info("❌ SERVICE - Producto NO encontrado por código: {}", codigoLimpio);
            }

            return producto;
        } catch (Exception e) {
            log.error("❌ ERROR SERVICE - Error buscando producto por código '{}': {}", codigo, e.getMessage());
            throw new RuntimeException("Error al buscar producto por código: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Búsqueda por ID con validación
    public Optional<Producto> buscarPorId(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                log.warn("⚠️ ID de producto vacío o nulo");
                return Optional.empty();
            }

            String idLimpio = id.trim();
            log.info("🔍 SERVICE - Buscando producto por ID: '{}'", idLimpio);

            Optional<Producto> producto = productoRepository.findById(idLimpio);

            if (producto.isPresent()) {
                log.info("✅ SERVICE - Producto encontrado por ID: {} - {}",
                        idLimpio, producto.get().getNombre());
            } else {
                log.warn("❌ SERVICE - Producto NO encontrado por ID: {}", idLimpio);
            }

            return producto;
        } catch (Exception e) {
            log.error("❌ ERROR SERVICE - Error buscando producto por ID '{}': {}", id, e.getMessage());
            throw new RuntimeException("Error al buscar producto por ID: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Guardar producto con validaciones completas
    public Producto guardarProducto(Producto producto) {
        try {
            log.info("💾 SERVICE - Guardando producto: {}", producto.getNombre());

            // ✅ VALIDACIONES COMPLETAS
            if (producto.getCodigo() == null || producto.getCodigo().trim().isEmpty()) {
                throw new IllegalArgumentException("El código del producto es obligatorio");
            }

            if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre del producto es obligatorio");
            }

            if (producto.getPrecio() <= 0) {
                throw new IllegalArgumentException("El precio debe ser mayor a 0");
            }

            if (producto.getCantidad() < 0) {
                throw new IllegalArgumentException("La cantidad no puede ser negativa");
            }

            if (producto.getCategoria() == null || producto.getCategoria().trim().isEmpty()) {
                throw new IllegalArgumentException("La categoría es obligatoria");
            }

            // Limpiar datos
            producto.setCodigo(producto.getCodigo().trim());
            producto.setNombre(producto.getNombre().trim());
            producto.setCategoria(producto.getCategoria().trim());

            // Validar código único para nuevos productos
            if (producto.getId() == null) {
                Optional<Producto> existente = productoRepository.findByCodigo(producto.getCodigo());
                if (existente.isPresent()) {
                    throw new IllegalArgumentException("Ya existe un producto con el código: " + producto.getCodigo());
                }
            }

            Producto productoGuardado = productoRepository.save(producto);
            log.info("✅ SERVICE - Producto guardado exitosamente: {} - {} (ID: {})",
                    productoGuardado.getCodigo(), productoGuardado.getNombre(), productoGuardado.getId());

            return productoGuardado;

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ SERVICE - Error de validación al guardar producto: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ ERROR SERVICE - Error inesperado al guardar producto: {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar el producto: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Eliminar producto con validaciones
    public void eliminarPorId(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("ID de producto inválido");
            }

            String idLimpio = id.trim();
            log.info("🗑️ SERVICE - Intentando eliminar producto ID: {}", idLimpio);

            if (!productoRepository.existsById(idLimpio)) {
                throw new NoSuchElementException("No se encontró producto con el ID: " + idLimpio);
            }

            productoRepository.deleteById(idLimpio);
            log.info("✅ SERVICE - Producto eliminado: {}", idLimpio);

        } catch (NoSuchElementException e) {
            log.warn("⚠️ SERVICE - Intento de eliminar producto inexistente: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("❌ ERROR SERVICE - Error al eliminar producto {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Error al eliminar producto: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Descontar stock con validaciones robustas
    public void descontarStock(String codigo, int cantidad) {
        try {
            if (codigo == null || codigo.trim().isEmpty()) {
                throw new IllegalArgumentException("Código de producto inválido");
            }

            if (cantidad <= 0) {
                throw new IllegalArgumentException("La cantidad a descontar debe ser mayor a 0");
            }

            String codigoLimpio = codigo.trim();
            Optional<Producto> productoOpt = productoRepository.findByCodigo(codigoLimpio);

            if (productoOpt.isPresent()) {
                Producto producto = productoOpt.get();

                if (producto.getCantidad() < cantidad) {
                    throw new RuntimeException("Stock insuficiente para: " + producto.getNombre() +
                            ". Stock disponible: " + producto.getCantidad() + ", solicitado: " + cantidad);
                }

                producto.setCantidad(producto.getCantidad() - cantidad);
                productoRepository.save(producto);

                log.info("📉 SERVICE - Stock descontado: {} - Cantidad: {}, Stock restante: {}",
                        producto.getNombre(), cantidad, producto.getCantidad());
            } else {
                throw new NoSuchElementException("No se encontró producto con el código: " + codigoLimpio);
            }
        } catch (IllegalArgumentException | NoSuchElementException e) {
            log.warn("⚠️ SERVICE - Error en descontarStock: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ ERROR SERVICE - Error inesperado en descontarStock: {}", e.getMessage(), e);
            throw new RuntimeException("Error al descontar stock: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Aumentar stock con validaciones
    public void aumentarStock(String codigo, int cantidad) {
        try {
            if (codigo == null || codigo.trim().isEmpty()) {
                throw new IllegalArgumentException("Código de producto inválido");
            }

            if (cantidad <= 0) {
                throw new IllegalArgumentException("La cantidad a aumentar debe ser mayor a 0");
            }

            String codigoLimpio = codigo.trim();
            Optional<Producto> productoOpt = productoRepository.findByCodigo(codigoLimpio);

            if (productoOpt.isPresent()) {
                Producto producto = productoOpt.get();
                producto.setCantidad(producto.getCantidad() + cantidad);
                productoRepository.save(producto);

                log.info("📈 SERVICE - Stock aumentado: {} - Cantidad: {}, Stock total: {}",
                        producto.getNombre(), cantidad, producto.getCantidad());
            } else {
                throw new NoSuchElementException("No se encontró producto con el código: " + codigoLimpio);
            }
        } catch (IllegalArgumentException | NoSuchElementException e) {
            log.warn("⚠️ SERVICE - Error en aumentarStock: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ ERROR SERVICE - Error inesperado en aumentarStock: {}", e.getMessage(), e);
            throw new RuntimeException("Error al aumentar stock: " + e.getMessage());
        }
    }

    public List<Producto> obtenerTodos() {
        try {
            List<Producto> productos = productoRepository.findAll();
            log.debug("📦 Obteniendo todos los productos: {}", productos.size());
            return productos;
        } catch (Exception e) {
            log.error("❌ Error al obtener todos los productos: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener productos: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Productos con stock bajo
    public List<Producto> obtenerProductosStockBajo(int stockMinimo) {
        try {
            if (stockMinimo < 0) {
                throw new IllegalArgumentException("El stock mínimo no puede ser negativo");
            }

            List<Producto> productosBajos = productoRepository.findAll().stream()
                    .filter(producto -> producto.getCantidad() <= stockMinimo)
                    .toList();

            log.info("⚠️ SERVICE - Productos con stock bajo (≤{}): {}", stockMinimo, productosBajos.size());
            return productosBajos;
        } catch (Exception e) {
            log.error("❌ Error obteniendo productos con stock bajo: {}", e.getMessage());
            throw new RuntimeException("Error al obtener productos con stock bajo: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Búsqueda por nombre para autocompletado
    public List<Producto> buscarPorNombre(String nombre) {
        try {
            if (nombre == null || nombre.trim().isEmpty()) {
                return List.of();
            }

            String nombreBusqueda = nombre.trim().toLowerCase();
            List<Producto> resultados = productoRepository.findAll().stream()
                    .filter(producto -> producto.getNombre().toLowerCase().contains(nombreBusqueda))
                    .limit(10)
                    .toList();

            log.debug("🔍 SERVICE - Autocompletado por '{}': {} resultados", nombreBusqueda, resultados.size());
            return resultados;
        } catch (Exception e) {
            log.error("❌ ERROR SERVICE - Error en búsqueda por nombre '{}': {}", nombre, e.getMessage());
            return List.of();
        }
    }

    // ✅ CORREGIDO: Verificar existencia por código
    public boolean existePorCodigo(String codigo) {
        try {
            if (codigo == null || codigo.trim().isEmpty()) {
                return false;
            }

            boolean existe = productoRepository.findByCodigo(codigo.trim()).isPresent();
            log.debug("🔍 SERVICE - Verificación existencia código '{}': {}", codigo, existe);
            return existe;
        } catch (Exception e) {
            log.error("❌ ERROR SERVICE - Error verificando existencia por código '{}': {}", codigo, e.getMessage());
            return false;
        }
    }

    // ✅ CORREGIDO: Productos por rango de precio
    public List<Producto> obtenerPorRangoPrecio(double precioMin, double precioMax) {
        try {
            if (precioMin < 0 || precioMax < 0) {
                throw new IllegalArgumentException("Los precios no pueden ser negativos");
            }

            if (precioMin > precioMax) {
                throw new IllegalArgumentException("El precio mínimo no puede ser mayor al máximo");
            }

            List<Producto> productos = productoRepository.findAll().stream()
                    .filter(producto -> producto.getPrecio() >= precioMin && producto.getPrecio() <= precioMax)
                    .toList();

            log.debug("💰 SERVICE - Productos en rango ${}-${}: {}", precioMin, precioMax, productos.size());
            return productos;
        } catch (Exception e) {
            log.error("❌ ERROR SERVICE - Error obteniendo productos por rango de precio: {}", e.getMessage());
            throw new RuntimeException("Error al obtener productos por rango de precio: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Actualizar costos de compra
    public int actualizarCostosCompra(double porcentajePrecioVenta) {
        try {
            if (porcentajePrecioVenta <= 0 || porcentajePrecioVenta >= 1) {
                throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 1 (exclusivo)");
            }

            List<Producto> productos = productoRepository.findAll();
            int actualizados = 0;

            for (Producto producto : productos) {
                if (producto.getCostoCompra() <= 0 && producto.getPrecio() > 0) {
                    double nuevoCosto = producto.getPrecio() * porcentajePrecioVenta;
                    producto.setCostoCompra(Math.round(nuevoCosto * 100.0) / 100.0);
                    productoRepository.save(producto);
                    actualizados++;

                    log.debug("💰 SERVICE - Costo actualizado: {} - Precio: ${}, Costo: ${}",
                            producto.getNombre(), producto.getPrecio(), producto.getCostoCompra());
                }
            }

            log.info("✅ SERVICE - Costos de compra actualizados: {} productos", actualizados);
            return actualizados;
        } catch (Exception e) {
            log.error("❌ ERROR SERVICE - Error actualizando costos de compra: {}", e.getMessage(), e);
            throw new RuntimeException("Error al actualizar costos de compra: " + e.getMessage());
        }
    }
}