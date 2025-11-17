package com.App.Lfarma.controller;

import com.App.Lfarma.DTO.PrediccionDemandaDTO;
import com.App.Lfarma.service.PrediccionDemandaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/predicciones")
public class PrediccionDemandaController {

    @Autowired
    private PrediccionDemandaService prediccionDemandaService;

    // Constructor para debug
    public PrediccionDemandaController() {
        System.out.println("✅ PrediccionDemandaController INICIALIZADO");
    }

    // Vista principal de predicciones
    @GetMapping
    public String mostrarPredicciones(Model model) {
        System.out.println("🎯 ACCEDIENDO A /predicciones");

        try {
            List<PrediccionDemandaDTO> predicciones = prediccionDemandaService.predecirDemandaTodosProductos();
            List<PrediccionDemandaDTO> alertas = prediccionDemandaService.obtenerProductosDemandaAlta();

            // Obtener estadísticas del servicio
            Map<String, Object> stats = prediccionDemandaService.obtenerEstadisticas();

            model.addAttribute("predicciones", predicciones);
            model.addAttribute("alertas", alertas);
            model.addAttribute("totalProductos", stats.get("totalProductos"));
            model.addAttribute("totalAlertas", stats.get("totalAlertas"));
            model.addAttribute("demandaAlta", stats.get("demandaAlta"));
            model.addAttribute("demandaMedia", stats.get("demandaMedia"));
            model.addAttribute("demandaBaja", stats.get("demandaBaja"));

            System.out.println("✅ Predicciones cargadas: " + predicciones.size());

        } catch (Exception e) {
            System.err.println("❌ ERROR en mostrarPredicciones: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("error", "Error cargando predicciones: " + e.getMessage());
            // Valores por defecto en caso de error
            model.addAttribute("predicciones", new ArrayList<>());
            model.addAttribute("alertas", new ArrayList<>());
            model.addAttribute("totalProductos", 0);
            model.addAttribute("totalAlertas", 0);
            model.addAttribute("demandaAlta", 0);
            model.addAttribute("demandaMedia", 0);
            model.addAttribute("demandaBaja", 0);
        }

        return "prediccion-demanda";
    }

    // Dashboard de predicciones - ✅ CORREGIDO
    @GetMapping("/dashboard")
    public String dashboardPredicciones(Model model) {
        System.out.println("🎯 ACCEDIENDO A /predicciones/dashboard");

        try {
            List<PrediccionDemandaDTO> predicciones = prediccionDemandaService.predecirDemandaTodosProductos();
            List<PrediccionDemandaDTO> alertas = prediccionDemandaService.obtenerProductosDemandaAlta();
            List<PrediccionDemandaDTO> recientes = prediccionDemandaService.obtenerPrediccionesRecientes();

            // Obtener estadísticas
            Map<String, Object> stats = prediccionDemandaService.obtenerEstadisticas();

            model.addAttribute("predicciones", predicciones);
            model.addAttribute("alertas", alertas);
            model.addAttribute("recientes", recientes);
            model.addAttribute("demandaAlta", stats.get("demandaAlta"));
            model.addAttribute("demandaMedia", stats.get("demandaMedia"));
            model.addAttribute("demandaBaja", stats.get("demandaBaja"));
            model.addAttribute("totalProductos", stats.get("totalProductos"));
            model.addAttribute("totalAlertas", stats.get("totalAlertas"));

            System.out.println("✅ Dashboard cargado - Predicciones: " + predicciones.size());
            System.out.println("✅ Alertas: " + alertas.size());
            System.out.println("✅ Recientes: " + recientes.size());

        } catch (Exception e) {
            System.err.println("❌ ERROR en dashboardPredicciones: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("error", "Error cargando dashboard: " + e.getMessage());

            // Valores por defecto para evitar errores en la vista
            model.addAttribute("predicciones", new ArrayList<>());
            model.addAttribute("alertas", new ArrayList<>());
            model.addAttribute("recientes", new ArrayList<>());
            model.addAttribute("demandaAlta", 0);
            model.addAttribute("demandaMedia", 0);
            model.addAttribute("demandaBaja", 0);
            model.addAttribute("totalProductos", 0);
            model.addAttribute("totalAlertas", 0);
        }

        return "dashboard-predicciones";
    }

    // API para re-entrenar el modelo
    @PostMapping("/api/reentrenar")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> reentrenarModelo() {
        System.out.println("🔄 SOLICITUD DE RE-ENTRENAMIENTO DEL MODELO");

        try {
            boolean exito = prediccionDemandaService.reentrenarModelo();

            Map<String, Object> response = new HashMap<>();
            response.put("success", exito);
            response.put("message", exito ? "Modelo re-entrenado exitosamente" : "Error re-entrenando el modelo");

            System.out.println("✅ Re-entrenamiento: " + (exito ? "EXITOSO" : "FALLIDO"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ ERROR en reentrenarModelo: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // Estado del modelo
    @GetMapping("/api/estado")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> obtenerEstadoModelo() {
        System.out.println("📊 SOLICITUD DE ESTADO DEL MODELO");

        try {
            Map<String, Object> estado = prediccionDemandaService.obtenerEstadoModelo();

            System.out.println("✅ Estado del modelo obtenido: " + estado);

            return ResponseEntity.ok(estado);
        } catch (Exception e) {
            System.err.println("❌ ERROR en obtenerEstadoModelo: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error obteniendo estado del modelo: " + e.getMessage()));
        }
    }

    // ✅ CORREGIDO: Endpoint para limpiar dataset ARFF con opciones
    @PostMapping("/api/limpiar-dataset")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> limpiarDataset(@RequestParam(defaultValue = "completa") String tipo) {
        System.out.println("🧹 SOLICITUD DE LIMPIEZA DEL DATASET ARFF - Tipo: " + tipo);

        try {
            Map<String, Object> resultado;

            if ("segura".equals(tipo)) {
                resultado = prediccionDemandaService.limpiarDatasetSeguro();
            } else {
                resultado = prediccionDemandaService.limpiarDatasetManual();
            }

            System.out.println("✅ Limpieza del dataset: " +
                    (Boolean.TRUE.equals(resultado.get("success")) ? "EXITOSA" : "FALLIDA"));

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            System.err.println("❌ ERROR en limpiarDataset: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Error limpiando dataset: " + e.getMessage()
                    ));
        }
    }

    // ✅ CORREGIDO: Endpoint para limpieza segura específica
    @PostMapping("/api/limpiar-dataset-seguro")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> limpiarDatasetSeguro() {
        System.out.println("🛡️ SOLICITUD DE LIMPIEZA SEGURA DEL DATASET");

        try {
            Map<String, Object> resultado = prediccionDemandaService.limpiarDatasetSeguro();

            System.out.println("✅ Limpieza segura del dataset: " +
                    (Boolean.TRUE.equals(resultado.get("success")) ? "EXITOSA" : "FALLIDA"));

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            System.err.println("❌ ERROR en limpiarDatasetSeguro: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Error en limpieza segura: " + e.getMessage()
                    ));
        }
    }

    // ✅ NUEVO: Endpoint para verificar productos reales vs dataset
    @GetMapping("/api/estado-filtro")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> obtenerEstadoFiltro() {
        System.out.println("🔍 SOLICITUD DE ESTADO DEL FILTRO");

        try {
            // Obtener todos los productos
            List<com.App.Lfarma.entity.Producto> todosProductos =
                    prediccionDemandaService.obtenerProductosRealesParaPrediccion();

            // Contar productos reales vs dataset
            int totalProductos = todosProductos.size();
            int productosReales = 0;
            int productosDataset = 0;

            List<Map<String, String>> listaDataset = new ArrayList<>();
            List<Map<String, String>> listaReales = new ArrayList<>();

            for (com.App.Lfarma.entity.Producto producto : todosProductos) {
                if (prediccionDemandaService.esProductoReal(producto)) {
                    productosReales++;
                    listaReales.add(Map.of(
                            "nombre", producto.getNombre(),
                            "codigo", producto.getCodigo(),
                            "tipo", "REAL"
                    ));
                } else {
                    productosDataset++;
                    listaDataset.add(Map.of(
                            "nombre", producto.getNombre(),
                            "codigo", producto.getCodigo(),
                            "tipo", "DATASET"
                    ));
                }
            }

            Map<String, Object> estado = new HashMap<>();
            estado.put("success", true);
            estado.put("totalProductos", totalProductos);
            estado.put("productosReales", productosReales);
            estado.put("productosDataset", productosDataset);
            estado.put("filtroActivo", true);
            estado.put("productosDatasetLista", listaDataset);
            estado.put("productosRealesLista", listaReales);
            estado.put("timestamp", java.time.LocalDateTime.now().toString());

            System.out.println("📊 Estado del filtro - Reales: " + productosReales +
                    ", Dataset: " + productosDataset + ", Total: " + totalProductos);

            return ResponseEntity.ok(estado);

        } catch (Exception e) {
            System.err.println("❌ ERROR en obtenerEstadoFiltro: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Error obteniendo estado del filtro: " + e.getMessage()
                    ));
        }
    }

    // ✅ NUEVO: Endpoint de prueba para verificar que el controlador funciona
    @GetMapping("/test")
    @ResponseBody
    public Map<String, String> test() {
        System.out.println("🧪 TEST ENDPOINT ACCEDIDO");

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "PrediccionDemandaController está funcionando correctamente");
        response.put("timestamp", java.time.LocalDateTime.now().toString());

        return response;
    }

    // ✅ NUEVO: Endpoint para obtener estadísticas rápidas
    @GetMapping("/api/estadisticas")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> obtenerEstadisticas() {
        try {
            Map<String, Object> stats = prediccionDemandaService.obtenerEstadisticas();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error obteniendo estadísticas: " + e.getMessage()));
        }
    }

    // ✅ NUEVO: Endpoint para obtener productos del dataset
    @GetMapping("/api/productos-dataset")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> obtenerProductosDataset() {
        System.out.println("📋 SOLICITUD DE LISTA DE PRODUCTOS DEL DATASET");

        try {
            List<com.App.Lfarma.entity.Producto> todosProductos =
                    prediccionDemandaService.obtenerProductosRealesParaPrediccion();

            List<Map<String, Object>> productosDataset = new ArrayList<>();
            List<Map<String, Object>> productosReales = new ArrayList<>();

            for (com.App.Lfarma.entity.Producto producto : todosProductos) {
                Map<String, Object> productoInfo = Map.of(
                        "id", producto.getId(),
                        "nombre", producto.getNombre(),
                        "codigo", producto.getCodigo(),
                        "precio", producto.getPrecio(),
                        "cantidad", producto.getCantidad(),
                        "categoria", producto.getCategoria()
                );

                if (prediccionDemandaService.esProductoReal(producto)) {
                    productosReales.add(productoInfo);
                } else {
                    productosDataset.add(productoInfo);
                }
            }

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("success", true);
            resultado.put("productosDataset", productosDataset);
            resultado.put("productosReales", productosReales);
            resultado.put("totalDataset", productosDataset.size());
            resultado.put("totalReales", productosReales.size());

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            System.err.println("❌ ERROR en obtenerProductosDataset: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ✅ NUEVO: Endpoint para limpiar solo predicciones
    @PostMapping("/api/limpiar-predicciones")
    @ResponseBody
    @CrossOrigin(origins = "*")
    public ResponseEntity<?> limpiarPredicciones() {
        System.out.println("🗑️ SOLICITUD DE LIMPIEZA DE PREDICCIONES");

        try {
            prediccionDemandaService.limpiarPrediccionesNoReales();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Predicciones limpiadas exitosamente",
                    "timestamp", java.time.LocalDateTime.now().toString()
            ));

        } catch (Exception e) {
            System.err.println("❌ ERROR en limpiarPredicciones: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}