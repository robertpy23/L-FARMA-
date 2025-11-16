package com.App.Lfarma.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.App.Lfarma.entity.Cliente;
import com.App.Lfarma.repository.ClienteRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ClienteService {

    private static final Logger log = LoggerFactory.getLogger(ClienteService.class);

    @Autowired
    private ClienteRepository clienteRepository;

    public List<Cliente> listarClientes() {
        try {
            List<Cliente> clientes = clienteRepository.findAll();
            log.info("📋 Listando {} clientes", clientes.size());
            return clientes;
        } catch (Exception e) {
            log.error("❌ Error al listar clientes: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener la lista de clientes: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Paginación para clientes
    public Page<Cliente> listarClientesPaginados(Pageable pageable) {
        try {
            Page<Cliente> clientesPage = clienteRepository.findAll(pageable);
            log.debug("📋 Clientes paginados - Página: {}, Tamaño: {}, Total: {}",
                    pageable.getPageNumber(), pageable.getPageSize(), clientesPage.getTotalElements());
            return clientesPage;
        } catch (Exception e) {
            log.error("❌ Error en listarClientesPaginados: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener clientes paginados: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Búsqueda de clientes por nombre con paginación
    public Page<Cliente> buscarClientesPorNombre(String nombre, Pageable pageable) {
        try {
            if (nombre == null || nombre.trim().isEmpty()) {
                log.debug("🔍 Búsqueda por nombre vacía - retornando todos los clientes");
                return clienteRepository.findAll(pageable);
            }

            String nombreLimpio = nombre.trim();
            Page<Cliente> clientesPage = clienteRepository.findByNombreContainingIgnoreCase(nombreLimpio, pageable);

            log.info("🔍 Búsqueda por nombre '{}': {} resultados", nombreLimpio, clientesPage.getNumberOfElements());
            return clientesPage;
        } catch (Exception e) {
            log.error("❌ Error en buscarClientesPorNombre '{}': {}", nombre, e.getMessage(), e);
            throw new RuntimeException("Error en búsqueda por nombre: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Búsqueda general en múltiples campos
    public Page<Cliente> buscarClientes(String searchTerm, Pageable pageable) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                log.debug("🔍 Búsqueda general vacía - retornando todos los clientes");
                return clienteRepository.findAll(pageable);
            }

            String terminoLimpio = searchTerm.trim();
            Page<Cliente> clientesPage = clienteRepository.buscarClientes(terminoLimpio, pageable);

            log.info("🔍 Búsqueda general '{}': {} resultados", terminoLimpio, clientesPage.getNumberOfElements());
            return clientesPage;
        } catch (Exception e) {
            log.error("❌ Error en buscarClientes '{}': {}", searchTerm, e.getMessage(), e);
            throw new RuntimeException("Error en búsqueda general: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Búsqueda combinada (nombre o código)
    public Page<Cliente> buscarClientesPorNombreOCodigo(String searchTerm, Pageable pageable) {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                log.debug("🔍 Búsqueda combinada vacía - retornando todos los clientes");
                return clienteRepository.findAll(pageable);
            }

            String terminoLimpio = searchTerm.trim();
            Page<Cliente> clientesPage = clienteRepository.findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(
                    terminoLimpio, terminoLimpio, pageable);

            log.info("🔍 Búsqueda combinada '{}': {} resultados", terminoLimpio, clientesPage.getNumberOfElements());
            return clientesPage;
        } catch (Exception e) {
            log.error("❌ Error en buscarClientesPorNombreOCodigo '{}': {}", searchTerm, e.getMessage(), e);
            throw new RuntimeException("Error en búsqueda combinada: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Obtener cliente por código
    public Optional<Cliente> obtenerClientePorCodigo(String codigo) {
        try {
            if (codigo == null || codigo.trim().isEmpty()) {
                log.warn("⚠️ Código de cliente vacío o nulo");
                return Optional.empty();
            }

            String codigoLimpio = codigo.trim();
            Optional<Cliente> cliente = clienteRepository.findByCodigo(codigoLimpio);

            if (cliente.isPresent()) {
                log.debug("🔍 Cliente encontrado por código: {}", codigoLimpio);
            } else {
                log.debug("🔍 Cliente NO encontrado por código: {}", codigoLimpio);
            }

            return cliente;
        } catch (Exception e) {
            log.error("❌ Error obteniendo cliente por código '{}': {}", codigo, e.getMessage());
            throw new RuntimeException("Error al obtener cliente por código: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Obtener cliente por ID
    public Optional<Cliente> obtenerClientePorId(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                log.warn("⚠️ ID de cliente vacío o nulo");
                return Optional.empty();
            }

            String idLimpio = id.trim();
            Optional<Cliente> cliente = clienteRepository.findById(idLimpio);

            if (cliente.isPresent()) {
                log.debug("🔍 Cliente encontrado por ID: {}", idLimpio);
            } else {
                log.debug("🔍 Cliente NO encontrado por ID: {}", idLimpio);
            }

            return cliente;
        } catch (Exception e) {
            log.error("❌ Error obteniendo cliente por ID '{}': {}", id, e.getMessage());
            throw new RuntimeException("Error al obtener cliente por ID: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Guardar cliente con validaciones robustas
    public Cliente guardarCliente(Cliente cliente) {
        try {
            log.info("💾 Guardando cliente: {}", cliente.getNombre());

            // ✅ VALIDACIONES COMPLETAS
            if (cliente.getCodigo() == null || cliente.getCodigo().trim().isEmpty()) {
                throw new IllegalArgumentException("El código del cliente es obligatorio");
            }

            if (cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
                throw new IllegalArgumentException("El nombre del cliente es obligatorio");
            }

            if (cliente.getUsername() == null || cliente.getUsername().trim().isEmpty()) {
                throw new IllegalArgumentException("El username del cliente es obligatorio");
            }

            // Limpiar datos
            cliente.setCodigo(cliente.getCodigo().trim());
            cliente.setNombre(cliente.getNombre().trim());
            cliente.setUsername(cliente.getUsername().trim());

            if (cliente.getEmail() != null) {
                cliente.setEmail(cliente.getEmail().trim().toLowerCase());
            }
            if (cliente.getTelefono() != null) {
                cliente.setTelefono(cliente.getTelefono().trim());
            }
            if (cliente.getDireccion() != null) {
                cliente.setDireccion(cliente.getDireccion().trim());
            }

            // ✅ VALIDACIÓN: Verificar código único
            if (cliente.getId() == null) {
                // Nuevo cliente - verificar que el código no exista
                Optional<Cliente> clienteExistente = clienteRepository.findByCodigo(cliente.getCodigo());
                if (clienteExistente.isPresent()) {
                    throw new IllegalArgumentException("Ya existe un cliente con el código: " + cliente.getCodigo());
                }
            } else {
                // Actualización - verificar que no haya otro cliente con el mismo código
                Optional<Cliente> clienteExistente = clienteRepository.findByCodigo(cliente.getCodigo());
                if (clienteExistente.isPresent() && !clienteExistente.get().getId().equals(cliente.getId())) {
                    throw new IllegalArgumentException("Ya existe otro cliente con el código: " + cliente.getCodigo());
                }
            }

            Cliente clienteGuardado = clienteRepository.save(cliente);
            log.info("✅ Cliente guardado exitosamente: {} - {}",
                    clienteGuardado.getCodigo(), clienteGuardado.getNombre());

            return clienteGuardado;

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Error de validación al guardar cliente: {}", e.getMessage());
            throw e; // Re-lanzar para manejo específico en el controller
        } catch (Exception e) {
            log.error("❌ Error inesperado al guardar cliente: {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar el cliente: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Agregar cliente (alias de guardarCliente con validación adicional)
    public Cliente agregarCliente(Cliente cliente) {
        try {
            log.info("➕ Agregando nuevo cliente: {}", cliente.getCodigo());

            // Validar que sea un nuevo cliente
            if (cliente.getId() != null) {
                throw new IllegalArgumentException("No se puede agregar un cliente existente. Use actualizar en su lugar.");
            }

            return guardarCliente(cliente);
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Error de validación al agregar cliente: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Error inesperado al agregar cliente: {}", e.getMessage(), e);
            throw new RuntimeException("Error al agregar cliente: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Eliminar cliente con validaciones
    public void eliminarCliente(String codigo) {
        try {
            if (codigo == null || codigo.trim().isEmpty()) {
                throw new IllegalArgumentException("Código de cliente inválido");
            }

            String codigoLimpio = codigo.trim();
            Optional<Cliente> clienteOpt = clienteRepository.findByCodigo(codigoLimpio);

            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                clienteRepository.deleteById(cliente.getId());
                log.info("🗑️ Cliente eliminado: {} - {}", codigoLimpio, cliente.getNombre());
            } else {
                throw new RuntimeException("No se encontró cliente con el código: " + codigoLimpio);
            }
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Error de validación al eliminar cliente: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Error al eliminar cliente {}: {}", codigo, e.getMessage(), e);
            throw new RuntimeException("Error al eliminar cliente: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Buscar por email
    public Optional<Cliente> buscarPorEmail(String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                return Optional.empty();
            }

            String emailLimpio = email.trim().toLowerCase();
            Optional<Cliente> cliente = clienteRepository.findByEmail(emailLimpio);

            log.debug("🔍 Búsqueda por email '{}': {}", emailLimpio, cliente.isPresent() ? "encontrado" : "no encontrado");
            return cliente;
        } catch (Exception e) {
            log.error("❌ Error buscando cliente por email '{}': {}", email, e.getMessage());
            throw new RuntimeException("Error al buscar cliente por email: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Obtener cliente por username
    public Optional<Cliente> obtenerClientePorUsername(String username) {
        try {
            if (username == null || username.trim().isEmpty()) {
                log.warn("⚠️ Username vacío o nulo");
                return Optional.empty();
            }

            String usernameLimpio = username.trim();
            Optional<Cliente> cliente = clienteRepository.findByUsername(usernameLimpio);

            if (cliente.isPresent()) {
                log.debug("🔍 Cliente encontrado por username: {}", usernameLimpio);
            } else {
                log.debug("🔍 Cliente NO encontrado por username: {}", usernameLimpio);
            }

            return cliente;
        } catch (Exception e) {
            log.error("❌ Error obteniendo cliente por username '{}': {}", username, e.getMessage());
            throw new RuntimeException("Error al obtener cliente por username: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Contar total de clientes
    public long contarTotalClientes() {
        try {
            long total = clienteRepository.count();
            log.debug("📊 Total de clientes en sistema: {}", total);
            return total;
        } catch (Exception e) {
            log.error("❌ Error contando total de clientes: {}", e.getMessage());
            throw new RuntimeException("Error al contar clientes: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Buscar clientes por teléfono
    public Optional<Cliente> buscarPorTelefono(String telefono) {
        try {
            if (telefono == null || telefono.trim().isEmpty()) {
                return Optional.empty();
            }

            String telefonoLimpio = telefono.trim();
            Optional<Cliente> cliente = clienteRepository.findByTelefono(telefonoLimpio);

            log.debug("🔍 Búsqueda por teléfono '{}': {}", telefonoLimpio, cliente.isPresent() ? "encontrado" : "no encontrado");
            return cliente;
        } catch (Exception e) {
            log.error("❌ Error buscando cliente por teléfono '{}': {}", telefono, e.getMessage());
            throw new RuntimeException("Error al buscar cliente por teléfono: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Búsqueda por teléfono con paginación
    public Page<Cliente> buscarPorTelefonoPaginado(String telefono, Pageable pageable) {
        try {
            if (telefono == null || telefono.trim().isEmpty()) {
                log.debug("🔍 Búsqueda por teléfono vacía - retornando todos los clientes");
                return clienteRepository.findAll(pageable);
            }

            String telefonoLimpio = telefono.trim();
            Page<Cliente> clientesPage = clienteRepository.findByTelefonoContaining(telefonoLimpio, pageable);

            log.info("🔍 Búsqueda por teléfono '{}': {} resultados", telefonoLimpio, clientesPage.getNumberOfElements());
            return clientesPage;
        } catch (Exception e) {
            log.error("❌ Error en buscarPorTelefonoPaginado '{}': {}", telefono, e.getMessage(), e);
            throw new RuntimeException("Error en búsqueda por teléfono: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Obtener clientes con dirección completa
    public Page<Cliente> obtenerClientesConDireccionCompleta(Pageable pageable) {
        try {
            Page<Cliente> clientesPage = clienteRepository.findClientesConDireccionCompleta(pageable);
            log.debug("📍 Clientes con dirección completa: {} resultados", clientesPage.getNumberOfElements());
            return clientesPage;
        } catch (Exception e) {
            log.error("❌ Error obteniendo clientes con dirección completa: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener clientes con dirección completa: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Versión lista para compatibilidad
    public List<Cliente> obtenerClientesConDireccion() {
        try {
            List<Cliente> clientes = clienteRepository.findClientesConDireccionCompleta(Pageable.unpaged()).getContent();
            log.debug("📍 Clientes con dirección completa (lista): {} resultados", clientes.size());
            return clientes;
        } catch (Exception e) {
            log.error("❌ Error obteniendo lista de clientes con dirección: {}", e.getMessage());
            throw new RuntimeException("Error al obtener lista de clientes con dirección: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Actualizar ubicación de cliente
    public Cliente actualizarUbicacionCliente(String codigo, Double latitud, Double longitud, String direccion) {
        try {
            if (codigo == null || codigo.trim().isEmpty()) {
                throw new IllegalArgumentException("Código de cliente inválido");
            }

            String codigoLimpio = codigo.trim();
            Optional<Cliente> clienteOpt = clienteRepository.findByCodigo(codigoLimpio);

            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                cliente.setLatitud(latitud);
                cliente.setLongitud(longitud);

                if (direccion != null && !direccion.trim().isEmpty()) {
                    cliente.setDireccion(direccion.trim());
                }

                Cliente clienteActualizado = clienteRepository.save(cliente);
                log.info("📍 Ubicación actualizada para cliente {}: Lat {}, Lng {}, Dir: {}",
                        codigoLimpio, latitud, longitud, direccion);

                return clienteActualizado;
            } else {
                throw new RuntimeException("Cliente no encontrado con código: " + codigoLimpio);
            }
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Error de validación al actualizar ubicación: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Error actualizando ubicación para cliente {}: {}", codigo, e.getMessage(), e);
            throw new RuntimeException("Error al actualizar ubicación del cliente: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Verificar si existe cliente por código
    public boolean existeClientePorCodigo(String codigo) {
        try {
            if (codigo == null || codigo.trim().isEmpty()) {
                return false;
            }

            boolean existe = clienteRepository.findByCodigo(codigo.trim()).isPresent();
            log.debug("🔍 Verificación existencia código '{}': {}", codigo, existe);
            return existe;
        } catch (Exception e) {
            log.error("❌ Error verificando existencia por código '{}': {}", codigo, e.getMessage());
            return false;
        }
    }

    // ✅ CORREGIDO: Obtener clientes recientes
    public Page<Cliente> obtenerClientesRecientes(Pageable pageable) {
        try {
            Page<Cliente> clientesPage = clienteRepository.findAllByOrderByCodigoDesc(pageable);
            log.debug("🕒 Clientes recientes: {} resultados", clientesPage.getNumberOfElements());
            return clientesPage;
        } catch (Exception e) {
            log.error("❌ Error obteniendo clientes recientes: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener clientes recientes: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Versión lista para compatibilidad
    public List<Cliente> obtenerClientesRecientes(int limite) {
        try {
            if (limite <= 0) {
                throw new IllegalArgumentException("El límite debe ser mayor a 0");
            }

            Pageable pageable = Pageable.ofSize(limite);
            List<Cliente> clientes = clienteRepository.findAllByOrderByCodigoDesc(pageable).getContent();

            log.debug("🕒 {} clientes recientes obtenidos", clientes.size());
            return clientes;
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Error de validación en obtenerClientesRecientes: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("❌ Error obteniendo clientes recientes: {}", e.getMessage());
            throw new RuntimeException("Error al obtener clientes recientes: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Búsqueda por dirección
    public Page<Cliente> buscarPorDireccion(String direccion, Pageable pageable) {
        try {
            if (direccion == null || direccion.trim().isEmpty()) {
                log.debug("🔍 Búsqueda por dirección vacía - retornando todos los clientes");
                return clienteRepository.findAll(pageable);
            }

            String direccionLimpia = direccion.trim();
            Page<Cliente> clientesPage = clienteRepository.findByDireccionContainingIgnoreCase(direccionLimpia, pageable);

            log.info("🔍 Búsqueda por dirección '{}': {} resultados", direccionLimpia, clientesPage.getNumberOfElements());
            return clientesPage;
        } catch (Exception e) {
            log.error("❌ Error en buscarPorDireccion '{}': {}", direccion, e.getMessage(), e);
            throw new RuntimeException("Error en búsqueda por dirección: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Estadísticas completas de clientes
    public Map<String, Object> obtenerEstadisticasClientes() {
        try {
            Map<String, Object> estadisticas = new HashMap<>();

            long totalClientes = clienteRepository.count();
            long conDireccion = clienteRepository.countClientesConDireccion();
            long sinDireccion = clienteRepository.countClientesSinDireccion();
            long conUbicacionCompleta = clienteRepository.countClientesConUbicacionCompleta();

            estadisticas.put("totalClientes", totalClientes);
            estadisticas.put("clientesConDireccion", conDireccion);
            estadisticas.put("clientesSinDireccion", sinDireccion);
            estadisticas.put("clientesConUbicacionCompleta", conUbicacionCompleta);

            // Calcular porcentajes
            if (totalClientes > 0) {
                estadisticas.put("porcentajeConDireccion", Math.round((conDireccion * 100.0) / totalClientes * 100.0) / 100.0);
                estadisticas.put("porcentajeConUbicacionCompleta", Math.round((conUbicacionCompleta * 100.0) / totalClientes * 100.0) / 100.0);
            } else {
                estadisticas.put("porcentajeConDireccion", 0.0);
                estadisticas.put("porcentajeConUbicacionCompleta", 0.0);
            }

            log.info("📊 Estadísticas de clientes generadas - Total: {}, Con dirección: {}, Con ubicación: {}",
                    totalClientes, conDireccion, conUbicacionCompleta);

            return estadisticas;
        } catch (Exception e) {
            log.error("❌ Error obteniendo estadísticas de clientes: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener estadísticas de clientes: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Búsqueda por código con paginación
    public Page<Cliente> buscarPorCodigoPaginado(String codigo, Pageable pageable) {
        try {
            if (codigo == null || codigo.trim().isEmpty()) {
                log.debug("🔍 Búsqueda por código vacía - retornando todos los clientes");
                return clienteRepository.findAll(pageable);
            }

            String codigoLimpio = codigo.trim();
            Page<Cliente> clientesPage = clienteRepository.findByCodigoContainingIgnoreCase(codigoLimpio, pageable);

            log.info("🔍 Búsqueda por código '{}': {} resultados", codigoLimpio, clientesPage.getNumberOfElements());
            return clientesPage;
        } catch (Exception e) {
            log.error("❌ Error en buscarPorCodigoPaginado '{}': {}", codigo, e.getMessage(), e);
            throw new RuntimeException("Error en búsqueda por código: " + e.getMessage());
        }
    }

    // ✅ CORREGIDO: Validar y limpiar datos del cliente antes de guardar
    public Cliente validarYGuardarCliente(Cliente cliente) {
        try {
            log.info("🧹 Validando y limpiando datos del cliente: {}", cliente.getCodigo());

            // Limpiar espacios en blanco
            if (cliente.getCodigo() != null) {
                cliente.setCodigo(cliente.getCodigo().trim());
            }
            if (cliente.getNombre() != null) {
                cliente.setNombre(cliente.getNombre().trim());
            }
            if (cliente.getUsername() != null) {
                cliente.setUsername(cliente.getUsername().trim());
            }
            if (cliente.getEmail() != null) {
                cliente.setEmail(cliente.getEmail().trim().toLowerCase());
            }
            if (cliente.getTelefono() != null) {
                cliente.setTelefono(cliente.getTelefono().trim());
            }
            if (cliente.getDireccion() != null) {
                cliente.setDireccion(cliente.getDireccion().trim());
            }

            Cliente clienteGuardado = guardarCliente(cliente);
            log.info("✅ Cliente validado y guardado exitosamente: {}", clienteGuardado.getCodigo());

            return clienteGuardado;

        } catch (Exception e) {
            log.error("❌ Error en validarYGuardarCliente: {}", e.getMessage(), e);
            throw new RuntimeException("Error al validar y guardar cliente: " + e.getMessage());
        }
    }
}