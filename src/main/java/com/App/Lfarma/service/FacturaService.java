package com.App.Lfarma.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import com.App.Lfarma.entity.Cliente;
import com.App.Lfarma.entity.DetalleFactura;
import com.App.Lfarma.entity.Factura;
import com.App.Lfarma.entity.Producto;
import com.App.Lfarma.repository.FacturaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FacturaService {

    private static final Logger log = LoggerFactory.getLogger(FacturaService.class);

    @Autowired
    private ProductoService productoService;

    @Autowired
    private FacturaRepository facturaRepository;

    // ✅ CORREGIDO: Crear factura con IVA y productos cargados correctamente
    public Factura crearFactura(Cliente cliente, List<DetalleFactura> detalles) {
        log.info("🧾 Creando factura para cliente: {}", cliente.getNombre());

        try {
            // ✅ VALIDACIONES INICIALES
            if (cliente == null) {
                throw new IllegalArgumentException("Cliente no puede ser nulo");
            }

            if (detalles == null || detalles.isEmpty()) {
                throw new IllegalArgumentException("La factura debe tener al menos un producto");
            }

            double subtotal = 0;
            double totalGanancia = 0;

            Factura factura = new Factura();
            factura.setFecha(new Date());
            factura.setCliente(cliente);

            // ✅✅✅ CORRECCIÓN CRÍTICA: Procesar detalles y cargar productos COMPLETOS
            List<DetalleFactura> detallesCompletos = new ArrayList<>();

            for (DetalleFactura detalle : detalles) {
                // ✅ Obtener el producto COMPLETO desde la base de datos
                Producto productoCompleto = null;

                if (detalle.getProducto() != null && detalle.getProducto().getId() != null) {
                    // Buscar por ID
                    productoCompleto = productoService.buscarPorId(detalle.getProducto().getId())
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + detalle.getProducto().getId()));
                } else if (detalle.getProducto() != null && detalle.getProducto().getCodigo() != null) {
                    // Buscar por código
                    productoCompleto = productoService.buscarPorCodigo(detalle.getProducto().getCodigo())
                            .orElseThrow(() -> new RuntimeException("Producto no encontrado con código: " + detalle.getProducto().getCodigo()));
                } else {
                    throw new IllegalArgumentException("Detalle de producto sin ID o código válido");
                }

                // Validar stock
                if (productoCompleto.getCantidad() < detalle.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente para: " + productoCompleto.getNombre() +
                            ". Stock disponible: " + productoCompleto.getCantidad() + ", solicitado: " + detalle.getCantidad());
                }

                // Validar precio
                if (productoCompleto.getPrecio() <= 0) {
                    throw new RuntimeException("El producto " + productoCompleto.getNombre() + " no tiene un precio de venta válido");
                }

                // ✅ CORREGIDO: Manejar productos sin costo de compra
                if (productoCompleto.getCostoCompra() <= 0) {
                    double costoCalculado = Math.round(productoCompleto.getPrecio() * 0.6 * 100.0) / 100.0;
                    log.warn("⚠️ Producto sin costo de compra: {}, usando costo calculado: {}",
                            productoCompleto.getNombre(), costoCalculado);
                    productoCompleto.setCostoCompra(costoCalculado);
                    productoService.guardarProducto(productoCompleto);
                }

                // Descontar stock
                productoService.descontarStock(productoCompleto.getCodigo(), detalle.getCantidad());

                // ✅ Crear nuevo detalle con el producto COMPLETO
                DetalleFactura detalleCompleto = new DetalleFactura();
                detalleCompleto.setProducto(productoCompleto);
                detalleCompleto.setCantidad(detalle.getCantidad());
                detalleCompleto.setPrecioUnitario(productoCompleto.getPrecio());

                detallesCompletos.add(detalleCompleto);

                // Calcular subtotal y ganancia
                double subtotalProducto = detalleCompleto.getCantidad() * detalleCompleto.getPrecioUnitario();
                subtotal += subtotalProducto;

                double gananciaProducto = detalleCompleto.getCantidad() *
                        (detalleCompleto.getPrecioUnitario() - productoCompleto.getCostoCompra());
                totalGanancia += gananciaProducto;

                log.debug("📦 Producto procesado: {} x {} = ${} (ganancia: ${})",
                        productoCompleto.getNombre(), detalleCompleto.getCantidad(), subtotalProducto, gananciaProducto);
            }

            // ✅✅✅ CORRECCIÓN: Usar los detalles COMPLETOS
            factura.setDetalles(detallesCompletos);

            // ✅ CORREGIDO: Calcular IVA (19%) y totales
            double ivaCalculado = Math.round(subtotal * 0.19 * 100.0) / 100.0;
            double totalConIva = Math.round((subtotal + ivaCalculado) * 100.0) / 100.0;

            // ✅ CONFIGURAR FACTURA CON IVA
            factura.setTotalVenta(Math.round(subtotal * 100.0) / 100.0);
            factura.setIva(ivaCalculado);
            factura.setTotal(totalConIva);
            factura.setGananciaNeta(Math.round(totalGanancia * 100.0) / 100.0);

            // ✅ Llamar al método calcularTotal para consistencia
            factura.calcularTotal();

            Factura facturaGuardada = facturaRepository.save(factura);

            log.info("✅ Factura creada exitosamente: {} - Subtotal: ${}, IVA: ${}, Total: ${}, Ganancia: ${}, Productos: {}",
                    facturaGuardada.getId(), facturaGuardada.getTotalVenta(), facturaGuardada.getIva(),
                    facturaGuardada.getTotal(), facturaGuardada.getGananciaNeta(), detallesCompletos.size());

            return facturaGuardada;

        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación al crear factura: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Error inesperado al crear factura: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear la factura: " + e.getMessage());
        }
    }

    // ✅✅✅ CORREGIDO: Obtener factura por ID con carga COMPLETA de productos
    public Optional<Factura> obtenerFacturaPorId(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                log.warn("⚠️ ID de factura vacío o nulo");
                return Optional.empty();
            }

            String idLimpio = id.trim();
            Optional<Factura> factura = facturaRepository.findById(idLimpio);

            if (factura.isPresent()) {
                Factura facturaEncontrada = factura.get();

                // ✅✅✅ CORRECCIÓN CRÍTICA: Cargar productos COMPLETOS en los detalles
                if (facturaEncontrada.getDetalles() != null) {
                    log.info("🔍 Cargando productos completos para factura {} ({} detalles)",
                            idLimpio, facturaEncontrada.getDetalles().size());

                    List<DetalleFactura> detallesCompletos = new ArrayList<>();

                    for (DetalleFactura detalle : facturaEncontrada.getDetalles()) {
                        if (detalle.getProducto() != null) {
                            // Intentar cargar el producto completo desde la base de datos
                            Optional<Producto> productoCompleto = Optional.empty();

                            // Buscar por ID si está disponible
                            if (detalle.getProducto().getId() != null) {
                                productoCompleto = productoService.buscarPorId(detalle.getProducto().getId());
                            }

                            // Si no se encuentra por ID, intentar por código
                            if (productoCompleto.isEmpty() && detalle.getProducto().getCodigo() != null) {
                                productoCompleto = productoService.buscarPorCodigo(detalle.getProducto().getCodigo());
                            }

                            if (productoCompleto.isPresent()) {
                                // ✅ Producto encontrado, actualizar el detalle
                                DetalleFactura detalleCompleto = new DetalleFactura();
                                detalleCompleto.setId(detalle.getId());
                                detalleCompleto.setProducto(productoCompleto.get());
                                detalleCompleto.setCantidad(detalle.getCantidad());
                                detalleCompleto.setPrecioUnitario(detalle.getPrecioUnitario());
                                detallesCompletos.add(detalleCompleto);

                                log.debug("✅ Producto cargado: {} (ID: {})",
                                        productoCompleto.get().getNombre(), productoCompleto.get().getId());
                            } else {
                                // ❌ Producto no encontrado, mantener el detalle original
                                log.warn("⚠️ Producto no encontrado en BD para detalle: {}",
                                        detalle.getProducto().getId() != null ?
                                                detalle.getProducto().getId() : detalle.getProducto().getCodigo());
                                detallesCompletos.add(detalle);
                            }
                        } else {
                            // ❌ Detalle sin producto, mantener como está
                            log.warn("⚠️ Detalle sin producto asociado en factura {}", idLimpio);
                            detallesCompletos.add(detalle);
                        }
                    }

                    // ✅ Actualizar la factura con los detalles completos
                    facturaEncontrada.setDetalles(detallesCompletos);
                }

                log.debug("🔍 Factura encontrada por ID: {}", idLimpio);
            } else {
                log.debug("🔍 Factura NO encontrada por ID: {}", idLimpio);
            }

            return factura;
        } catch (Exception e) {
            log.error("❌ Error obteniendo factura por ID '{}': {}", id, e.getMessage());
            throw new RuntimeException("Error al obtener factura por ID: " + e.getMessage());
        }
    }

    // ... (el resto de los métodos se mantienen igual) ...

    public List<Factura> listarFacturas() {
        try {
            List<Factura> facturas = facturaRepository.findAll();
            log.debug("📋 Listando {} facturas", facturas.size());
            return facturas;
        } catch (Exception e) {
            log.error("❌ Error al listar facturas: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener la lista de facturas: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Paginación para facturas
    public Page<Factura> listarFacturasPaginadas(Pageable pageable) {
        try {
            Page<Factura> facturasPage = facturaRepository.findAll(pageable);
            log.debug("📋 Facturas paginadas - Página: {}, Tamaño: {}, Total: {}",
                    pageable.getPageNumber(), pageable.getPageSize(), facturasPage.getTotalElements());
            return facturasPage;
        } catch (Exception e) {
            log.error("❌ Error en listarFacturasPaginadas: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener facturas paginadas: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Búsqueda de facturas con manejo robusto
    public List<Factura> buscarFacturas(String searchTerm) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                log.debug("🔍 Búsqueda de facturas sin término - retornando todas");
                return facturaRepository.findAll();
            }

            String searchLower = searchTerm.trim().toLowerCase();

            List<Factura> facturasFiltradas = facturaRepository.findAll().stream()
                    .filter(factura -> {
                        try {
                            // ✅ VALIDACIÓN CRÍTICA: Verificar que cliente no sea null
                            if (factura.getCliente() == null) {
                                log.warn("⚠️ Factura sin cliente: {}", factura.getId());
                                return false;
                            }

                            boolean matchesId = factura.getId().toLowerCase().contains(searchLower);
                            boolean matchesNombre = factura.getCliente().getNombre() != null &&
                                    factura.getCliente().getNombre().toLowerCase().contains(searchLower);
                            boolean matchesIdentificacion = factura.getCliente().getIdentificacion() != null &&
                                    factura.getCliente().getIdentificacion().toLowerCase().contains(searchLower);
                            boolean matchesCodigo = factura.getCliente().getCodigo() != null &&
                                    factura.getCliente().getCodigo().toLowerCase().contains(searchLower);

                            return matchesId || matchesNombre || matchesIdentificacion || matchesCodigo;

                        } catch (Exception e) {
                            log.warn("⚠️ Error procesando factura {} en búsqueda: {}", factura.getId(), e.getMessage());
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

            log.info("🔍 Búsqueda de facturas '{}': {} resultados", searchTerm, facturasFiltradas.size());
            return facturasFiltradas;

        } catch (Exception e) {
            log.error("❌ Error en buscarFacturas '{}': {}", searchTerm, e.getMessage(), e);
            throw new RuntimeException("Error en la búsqueda de facturas: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Búsqueda con paginación más eficiente
    public Page<Factura> buscarFacturasPaginadas(String searchTerm, Pageable pageable) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                log.debug("🔍 Búsqueda paginada sin término - retornando todas");
                return facturaRepository.findAll(pageable);
            }

            List<Factura> facturasFiltradas = buscarFacturas(searchTerm);

            // Calcular índices para la página actual
            int start = Math.min((int) pageable.getOffset(), facturasFiltradas.size());
            int end = Math.min((start + pageable.getPageSize()), facturasFiltradas.size());

            List<Factura> pageContent = facturasFiltradas.subList(start, end);

            log.debug("🔍 Búsqueda paginada '{}': {} resultados (página {})",
                    searchTerm, facturasFiltradas.size(), pageable.getPageNumber() + 1);

            return new PageImpl<>(pageContent, pageable, facturasFiltradas.size());

        } catch (Exception e) {
            log.error("❌ Error en buscarFacturasPaginadas '{}': {}", searchTerm, e.getMessage(), e);
            throw new RuntimeException("Error en la búsqueda paginada de facturas: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Buscar facturas por rango de fechas
    public List<Factura> buscarFacturasPorFecha(Date desde, Date hasta) {
        try {
            if (desde == null || hasta == null) {
                throw new IllegalArgumentException("Las fechas desde y hasta son requeridas");
            }

            if (desde.after(hasta)) {
                throw new IllegalArgumentException("La fecha 'desde' no puede ser posterior a 'hasta'");
            }

            List<Factura> facturas = facturaRepository.findByFechaBetween(desde, hasta);
            log.info("📅 Facturas por fecha {}-{}: {} resultados", desde, hasta, facturas.size());
            return facturas;
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Error de validación en buscarFacturasPorFecha: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Error en buscarFacturasPorFecha: {}", e.getMessage(), e);
            throw new RuntimeException("Error al buscar facturas por fecha: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Contar total de facturas
    public long contarTotalFacturas() {
        try {
            long total = facturaRepository.count();
            log.debug("📊 Total de facturas en sistema: {}", total);
            return total;
        } catch (Exception e) {
            log.error("❌ Error contando total de facturas: {}", e.getMessage());
            throw new RuntimeException("Error al contar facturas: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Obtener facturas recientes
    public List<Factura> obtenerFacturasRecientes(int limite) {
        try {
            if (limite <= 0) {
                throw new IllegalArgumentException("El límite debe ser mayor a 0");
            }

            Pageable pageable = PageRequest.of(0, limite, Sort.by(Sort.Direction.DESC, "fecha"));
            List<Factura> facturasRecientes = facturaRepository.findAll(pageable).getContent();

            log.debug("🕒 {} facturas recientes obtenidas", facturasRecientes.size());
            return facturasRecientes;
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Error de validación en obtenerFacturasRecientes: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Error en obtenerFacturasRecientes: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener facturas recientes: " + e.getMessage());
        }
    }

    // ✅ NUEVO MÉTODO: Obtener estadísticas de facturas
    public Map<String, Object> obtenerEstadisticasFacturas() {
        try {
            Map<String, Object> estadisticas = new HashMap<>();

            long totalFacturas = facturaRepository.count();
            List<Factura> facturasRecientes = obtenerFacturasRecientes(5);

            // Calcular total de ventas y ganancias
            double totalVentas = 0;
            double totalGanancias = 0;

            List<Factura> todasFacturas = facturaRepository.findAll();
            for (Factura factura : todasFacturas) {
                totalVentas += factura.getTotal();
                totalGanancias += factura.getGananciaNeta();
            }

            estadisticas.put("totalFacturas", totalFacturas);
            estadisticas.put("totalVentas", Math.round(totalVentas * 100.0) / 100.0);
            estadisticas.put("totalGanancias", Math.round(totalGanancias * 100.0) / 100.0);
            estadisticas.put("facturasRecientes", facturasRecientes);
            estadisticas.put("promedioVenta", totalFacturas > 0 ? Math.round((totalVentas / totalFacturas) * 100.0) / 100.0 : 0);

            log.info("📊 Estadísticas de facturas generadas - Total: {}, Ventas: ${}, Ganancias: ${}",
                    totalFacturas, totalVentas, totalGanancias);

            return estadisticas;
        } catch (Exception e) {
            log.error("❌ Error obteniendo estadísticas de facturas: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener estadísticas de facturas: " + e.getMessage());
        }
    }

    // ✅ NUEVO MÉTODO: Verificar si una factura existe
    public boolean existeFactura(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return false;
            }
            boolean existe = facturaRepository.existsById(id.trim());
            log.debug("🔍 Verificación existencia factura {}: {}", id, existe);
            return existe;
        } catch (Exception e) {
            log.error("❌ Error verificando existencia de factura {}: {}", id, e.getMessage());
            return false;
        }
    }
}