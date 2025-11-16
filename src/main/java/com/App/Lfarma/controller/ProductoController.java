package com.App.Lfarma.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.App.Lfarma.entity.Producto;
import com.App.Lfarma.service.ProductoService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private static final Logger log = LoggerFactory.getLogger(ProductoController.class);

    @Autowired
    private ProductoService productoService;

    private List<String> obtenerCategorias() {
        return Arrays.asList("Medicamento", "Higiene", "Cosmético", "Suplemento", "Otros");
    }

    // ✅ MÉTODO PARA VERIFICAR SI EL USUARIO ACTUAL ES ADMIN
    private boolean esAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }

    // ✅ PASAR EL ATRIBUTO A TODAS LAS VISTAS
    @ModelAttribute("esAdmin")
    public boolean esAdminAttribute() {
        return esAdmin();
    }

    // ✅✅✅ CORREGIDO: Método principal - SOLUCIÓN DEFINITIVA
    @GetMapping
    public String listarProductos(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String categoria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "nombre") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {

        log.info("🎯 INICIANDO CARGA DE PRODUCTOS - Página: {}, Tamaño: {}", page, size);

        List<String> categorias = obtenerCategorias();
        model.addAttribute("categorias", categorias);

        try {
            // ✅ Validar parámetros
            if (page < 0) page = 0;
            if (size <= 0) size = 12;
            if (size > 50) size = 50;

            Sort sort = Sort.by(sortBy);
            sort = sortDir.equalsIgnoreCase("asc") ? sort.ascending() : sort.descending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Producto> pageProductos;

            // ✅✅✅ CORRECCIÓN CRÍTICA: Usar los métodos CORRECTOS del servicio
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String terminoBusqueda = searchTerm.trim();
                log.info("🔍 Búsqueda activada: '{}'", terminoBusqueda);
                pageProductos = productoService.buscarProductos(terminoBusqueda, categoria, pageable);
                model.addAttribute("searchTerm", terminoBusqueda);
            } else if (categoria != null && !categoria.isEmpty()) {
                log.info("📂 Filtrando por categoría: '{}'", categoria);
                pageProductos = productoService.listarProductosPaginadas(categoria, pageable);
                model.addAttribute("categoria", categoria);
            } else {
                log.info("📋 Listando todos los productos");
                pageProductos = productoService.listarProductosPaginadas(pageable);
            }

            // ✅✅✅ DEBUG CRÍTICO - Verificar qué está pasando
            log.info("🔍 DEBUG CONTROLADOR - Resultados obtenidos:");
            log.info("   - Total elementos en BD: {}", pageProductos.getTotalElements());
            log.info("   - Elementos en esta página: {}", pageProductos.getNumberOfElements());
            log.info("   - Contenido size: {}", pageProductos.getContent().size());
            log.info("   - Página actual: {} de {}", page, pageProductos.getTotalPages());

            // ✅✅✅ VERIFICAR CADA PRODUCTO INDIVIDUALMENTE
            List<Producto> productosContent = pageProductos.getContent();
            log.info("🔍 PRODUCTOS ENCONTRADOS EN EL CONTROLLER:");
            if (!productosContent.isEmpty()) {
                for (int i = 0; i < productosContent.size(); i++) {
                    Producto p = productosContent.get(i);
                    log.info("   {}. {} (ID: {}, Código: {})",
                            i + 1, p.getNombre(), p.getId(), p.getCodigo());
                }
            } else {
                log.warn("⚠️ NO SE ENCONTRARON PRODUCTOS EN EL CONTROLLER");
            }

            // ✅✅✅ CORRECCIÓN: Enviar el contenido COMPLETO de la página
            model.addAttribute("productos", productosContent);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", pageProductos.getTotalPages());
            model.addAttribute("totalItems", pageProductos.getTotalElements());
            model.addAttribute("pageSize", size);
            model.addAttribute("sortField", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");

            log.info("✅ CONTROLADOR - Productos enviados a la vista: {} productos", productosContent.size());

        } catch (Exception e) {
            log.error("❌ Error crítico en listarProductos: {}", e.getMessage(), e);
            model.addAttribute("error", "Error al cargar productos: " + e.getMessage());
            model.addAttribute("productos", List.of());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalItems", 0);
            model.addAttribute("pageSize", size);
        }

        return "visualizar-productos";
    }

    // ✅✅✅ ENDPOINT DE DIAGNÓSTICO - Para verificar datos
    @GetMapping("/debug")
    @ResponseBody
    public ResponseEntity<?> debugProductos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        try {
            log.info("🔍 EJECUTANDO DIAGNÓSTICO - Página: {}, Tamaño: {}", page, size);

            Pageable pageable = PageRequest.of(page, size);
            Page<Producto> pageProductos = productoService.listarProductosPaginadas(pageable);

            Map<String, Object> resultado = Map.of(
                    "totalElementos", pageProductos.getTotalElements(),
                    "elementosEnPagina", pageProductos.getNumberOfElements(),
                    "contenidoSize", pageProductos.getContent().size(),
                    "productos", pageProductos.getContent().stream().map(p -> Map.of(
                            "id", p.getId(),
                            "codigo", p.getCodigo(),
                            "nombre", p.getNombre(),
                            "precio", p.getPrecio(),
                            "cantidad", p.getCantidad(),
                            "categoria", p.getCategoria()
                    )).toList(),
                    "paginaActual", page,
                    "totalPaginas", pageProductos.getTotalPages()
            );

            log.info("✅ DIAGNÓSTICO - Resultados: {}", resultado);

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            log.error("❌ Error en diagnóstico: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(Map.of("error", e.getMessage(), "tipo", e.getClass().getSimpleName()));
        }
    }

    // ✅✅✅ VERSIÓN SIN PAGINACIÓN - Para test
    @GetMapping("/todos")
    public String listarTodosProductos(Model model) {
        try {
            log.info("🔍 CARGANDO TODOS LOS PRODUCTOS SIN PAGINACIÓN");

            List<Producto> todosProductos = productoService.listarProductos();

            log.info("📊 TODOS LOS PRODUCTOS - Total encontrados: {}", todosProductos.size());

            // Verificar cada producto
            if (!todosProductos.isEmpty()) {
                log.info("🔍 DETALLE DE PRODUCTOS ENCONTRADOS:");
                for (int i = 0; i < Math.min(todosProductos.size(), 10); i++) {
                    Producto p = todosProductos.get(i);
                    log.info("   {}. {} (ID: {}, Código: {})",
                            i + 1, p.getNombre(), p.getId(), p.getCodigo());
                }
            }

            model.addAttribute("productos", todosProductos);
            model.addAttribute("totalItems", todosProductos.size());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 1);
            model.addAttribute("pageSize", todosProductos.size());

            log.info("✅ TODOS LOS PRODUCTOS - Enviados a vista: {} elementos", todosProductos.size());

        } catch (Exception e) {
            log.error("❌ ERROR cargando todos los productos: {}", e.getMessage(), e);
            model.addAttribute("error", "Error: " + e.getMessage());
            model.addAttribute("productos", List.of());
        }

        return "visualizar-productos";
    }

    @GetMapping("/registrar-productos")
    public String mostrarFormularioRegistro(Model model) {
        if (!esAdmin()) {
            return "redirect:/productos?error=No tienes permisos para registrar productos";
        }
        model.addAttribute("producto", new Producto());
        model.addAttribute("categorias", obtenerCategorias());
        return "registrar-productos";
    }

    // ✅ CORREGIDO: Mejor manejo de redirecciones y mensajes flash
    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Producto producto,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (!esAdmin()) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para guardar productos");
            return "redirect:/productos";
        }

        if (result.hasErrors()) {
            model.addAttribute("categorias", obtenerCategorias());
            model.addAttribute("error", "Por favor, verifica los datos del formulario.");
            return "registrar-productos";
        }

        try {
            if (producto.getId() == null) {
                Optional<Producto> productoExistente = productoService.buscarPorCodigo(producto.getCodigo());
                if (productoExistente.isPresent()) {
                    model.addAttribute("error", "Ya existe un producto con el código: " + producto.getCodigo());
                    model.addAttribute("categorias", obtenerCategorias());
                    return "registrar-productos";
                }
            }

            Producto productoGuardado = productoService.guardarProducto(producto);
            redirectAttributes.addFlashAttribute("success",
                    "Producto '" + productoGuardado.getNombre() + "' registrado exitosamente");

            log.info("✅ Producto guardado: {} - {}", productoGuardado.getCodigo(), productoGuardado.getNombre());

            return "redirect:/productos";

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Error de validación al guardar producto: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categorias", obtenerCategorias());
            return "registrar-productos";
        } catch (Exception e) {
            log.error("❌ Error inesperado al guardar producto: {}", e.getMessage(), e);
            model.addAttribute("error", "Error inesperado al guardar el producto: " + e.getMessage());
            model.addAttribute("categorias", obtenerCategorias());
            return "registrar-productos";
        }
    }

    @GetMapping("/actualizar-productos")
    public String mostrarFormularioEditar(@RequestParam String id, Model model, RedirectAttributes redirectAttributes) {
        if (!esAdmin()) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para editar productos");
            return "redirect:/productos";
        }
        try {
            Optional<Producto> producto = productoService.buscarPorId(id);
            if (producto.isPresent()) {
                model.addAttribute("producto", producto.get());
                model.addAttribute("categorias", obtenerCategorias());
                log.info("📝 Cargando formulario de edición para producto: {}", producto.get().getNombre());
                return "actualizar-productos";
            } else {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                return "redirect:/productos";
            }
        } catch (Exception e) {
            log.error("❌ Error al cargar producto para edición: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al cargar producto: " + e.getMessage());
            return "redirect:/productos";
        }
    }

    // ✅ CORREGIDO: Mejor manejo de actualización
    @PostMapping("/actualizar")
    public String actualizarProducto(@ModelAttribute Producto producto,
                                     BindingResult result,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        if (!esAdmin()) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para actualizar productos");
            return "redirect:/productos";
        }

        if (result.hasErrors()) {
            model.addAttribute("categorias", obtenerCategorias());
            model.addAttribute("error", "Por favor, verifica los datos del formulario.");
            return "actualizar-productos";
        }

        try {
            Producto productoActualizado = productoService.guardarProducto(producto);
            redirectAttributes.addFlashAttribute("success",
                    "Producto '" + productoActualizado.getNombre() + "' actualizado exitosamente");

            log.info("✅ Producto actualizado: {} - {}", productoActualizado.getCodigo(), productoActualizado.getNombre());

            return "redirect:/productos";

        } catch (IllegalArgumentException e) {
            log.warn("⚠️ Error de validación al actualizar producto: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            model.addAttribute("categorias", obtenerCategorias());
            return "actualizar-productos";
        } catch (Exception e) {
            log.error("❌ Error inesperado al actualizar producto: {}", e.getMessage(), e);
            model.addAttribute("error", "Error inesperado al actualizar el producto: " + e.getMessage());
            model.addAttribute("categorias", obtenerCategorias());
            return "actualizar-productos";
        }
    }

    // ✅ CORREGIDO: Mejor manejo de eliminación
    @PostMapping("/eliminar")
    public String eliminarProducto(@RequestParam String id, RedirectAttributes redirectAttributes) {
        if (!esAdmin()) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para eliminar productos");
            return "redirect:/productos";
        }

        try {
            Optional<Producto> producto = productoService.buscarPorId(id);
            if (producto.isPresent()) {
                String nombreProducto = producto.get().getNombre();
                productoService.eliminarPorId(id);
                redirectAttributes.addFlashAttribute("success",
                        "Producto '" + nombreProducto + "' eliminado exitosamente");

                log.info("🗑️ Producto eliminado: {} - {}", id, nombreProducto);
            } else {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
            }
        } catch (Exception e) {
            log.error("❌ Error al eliminar producto {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al eliminar producto: " + e.getMessage());
        }

        return "redirect:/productos";
    }

    @GetMapping("/buscar-productos")
    public String mostrarFormularioBuscar(Model model) {
        model.addAttribute("producto", new Producto());
        return "buscar-productos";
    }

    @PostMapping("/buscar")
    public String buscarProductoPorCodigo(@RequestParam String codigoProducto, Model model) {
        try {
            Optional<Producto> producto = productoService.buscarPorCodigo(codigoProducto.trim());

            if (producto.isPresent()) {
                model.addAttribute("producto", producto.get());
                log.info("🔍 Producto encontrado: {}", producto.get().getNombre());
                return "resultado-busqueda";
            } else {
                model.addAttribute("mensaje", "Producto no encontrado con código: " + codigoProducto);
                return "buscar-productos";
            }
        } catch (Exception e) {
            log.error("❌ Error en la búsqueda de producto: {}", e.getMessage());
            model.addAttribute("mensaje", "Error en la búsqueda: " + e.getMessage());
            return "buscar-productos";
        }
    }

    // ✅ CORREGIDO: Mejor manejo de subida de imágenes
    @PostMapping("/{id}/imagen")
    public String subirImagen(@PathVariable String id,
                              @RequestParam("file") MultipartFile file,
                              RedirectAttributes redirectAttributes) {
        // ✅ PROTEGER: Solo admin puede subir imágenes
        if (!esAdmin()) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para subir imágenes");
            return "redirect:/productos";
        }

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Debes seleccionar una imagen.");
            return "redirect:/productos";
        }

        try {
            // Validar tipo de archivo
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("error", "Solo se permiten archivos de imagen.");
                return "redirect:/productos";
            }

            // Validar tamaño del archivo (máximo 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("error", "La imagen no puede ser mayor a 5MB.");
                return "redirect:/productos";
            }

            // Crear carpeta si no existe
            Path directorio = Paths.get("src/main/resources/static/images");
            if (!Files.exists(directorio)) {
                Files.createDirectories(directorio);
            }

            // Guardar archivo con nombre seguro
            String nombreOriginal = file.getOriginalFilename();
            String extension = "";
            if (nombreOriginal != null && nombreOriginal.contains(".")) {
                extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
            }

            String nombreArchivo = "producto_" + id + "_" + System.currentTimeMillis() + extension;
            Path ruta = directorio.resolve(nombreArchivo);
            Files.copy(file.getInputStream(), ruta, StandardCopyOption.REPLACE_EXISTING);

            // Actualizar producto en Mongo
            Producto producto = productoService.buscarPorId(id)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
            producto.setImagen("/images/" + nombreArchivo);
            productoService.guardarProducto(producto);

            redirectAttributes.addFlashAttribute("success", "Imagen subida correctamente.");
            log.info("🖼️ Imagen subida para producto {}: {}", id, nombreArchivo);

        } catch (IOException e) {
            log.error("❌ Error de IO al subir imagen: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al subir la imagen: " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ Error inesperado al subir imagen: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Error inesperado: " + e.getMessage());
        }

        return "redirect:/productos";
    }

    // ✅ CORREGIDO: Endpoint para servir imágenes con mejor manejo de errores
    @GetMapping("/images/{imageName:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String imageName) {
        try {
            log.debug("🔍 Buscando imagen: {}", imageName);

            // Buscar en static/images/
            Resource resource = new ClassPathResource("static/images/" + imageName);

            if (resource.exists() && resource.isReadable()) {
                log.debug("✅ Imagen encontrada: {}", imageName);

                // Determinar el tipo de contenido basado en la extensión del archivo
                MediaType mediaType = MediaType.IMAGE_JPEG;
                if (imageName.toLowerCase().endsWith(".png")) {
                    mediaType = MediaType.IMAGE_PNG;
                } else if (imageName.toLowerCase().endsWith(".gif")) {
                    mediaType = MediaType.IMAGE_GIF;
                } else if (imageName.toLowerCase().endsWith(".webp")) {
                    mediaType = MediaType.parseMediaType("image/webp");
                }

                return ResponseEntity.ok()
                        .contentType(mediaType)
                        .body(resource);
            } else {
                log.warn("❌ Imagen NO encontrada o no legible: static/images/{}", imageName);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("❌ Error cargando imagen {}: {}", imageName, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ CORREGIDO: Endpoint API con métodos que SÍ EXISTEN
    @GetMapping("/api")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> listarProductosApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            // Validar parámetros
            if (page < 0) page = 0;
            if (size <= 0) size = 20;
            if (size > 100) size = 100;

            Pageable pageable = PageRequest.of(page, size);
            // ✅ MÉTODO QUE SÍ EXISTE
            Page<Producto> productosPage = productoService.listarProductosPaginadas(pageable);

            log.info("✅ API Productos - Página: {}, Tamaño: {}, Total: {}",
                    page, size, productosPage.getTotalElements());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "productos", productosPage.getContent(),
                    "currentPage", page,
                    "totalPages", productosPage.getTotalPages(),
                    "totalItems", productosPage.getTotalElements(),
                    "pageSize", size
            ));
        } catch (Exception e) {
            log.error("❌ Error en /productos/api: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "Error al cargar productos: " + e.getMessage()
            ));
        }
    }

    // ✅ CORREGIDO: Endpoint para productos destacados
    @GetMapping("/api/destacados")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> obtenerProductosDestacados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "precio"));
            // ✅ MÉTODO QUE SÍ EXISTE
            Page<Producto> productosPage = productoService.listarProductosPaginadas(pageable);

            // Filtrar para mostrar solo productos con stock
            List<Producto> productosConStock = productosPage.getContent().stream()
                    .filter(p -> p.getCantidad() > 0)
                    .limit(6)
                    .toList();

            log.info("✅ API Productos Destacados - Encontrados: {}", productosConStock.size());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "productos", productosConStock,
                    "total", productosConStock.size()
            ));
        } catch (Exception e) {
            log.error("❌ Error en /productos/api/destacados: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "Error al cargar productos destacados: " + e.getMessage()
            ));
        }
    }

    // ✅ CORREGIDO: Endpoint para producto por ID
    @GetMapping("/api/{id}")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> obtenerProductoPorId(@PathVariable String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "ID de producto inválido"
                ));
            }

            Producto producto = productoService.buscarPorId(id)
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "producto", producto
            ));
        } catch (Exception e) {
            log.error("❌ Error en /productos/api/{}: {}", id, e.getMessage());
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    // ✅ CORREGIDO: Endpoint para productos por categoría
    @GetMapping("/api/categoria/{categoria}")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> obtenerProductosPorCategoria(
            @PathVariable String categoria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            // ✅ MÉTODO QUE SÍ EXISTE
            Page<Producto> productosPage = productoService.listarProductosPaginadas(categoria, pageable);

            log.info("✅ API Productos por Categoría '{}' - Total: {}", categoria, productosPage.getTotalElements());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "productos", productosPage.getContent(),
                    "currentPage", page,
                    "totalPages", productosPage.getTotalPages(),
                    "totalItems", productosPage.getTotalElements(),
                    "categoria", categoria
            ));
        } catch (Exception e) {
            log.error("❌ Error en /productos/api/categoria/{}: {}", categoria, e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "Error al cargar productos por categoría: " + e.getMessage()
            ));
        }
    }
}