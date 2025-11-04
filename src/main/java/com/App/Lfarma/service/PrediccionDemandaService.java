package com.App.Lfarma.service;

import com.App.Lfarma.entity.PrediccionDemanda;
import com.App.Lfarma.entity.Producto;
import com.App.Lfarma.DTO.PrediccionDemandaDTO;
import com.App.Lfarma.repository.PrediccionDemandaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PrediccionDemandaService {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private PrediccionDemandaRepository prediccionDemandaRepository;

    private Classifier classifier;
    private Instances dataStructure;
    private boolean modeloTrained = false;

    /**
     * Entrenar modelo de forma robusta
     */
    public synchronized void entrenarModelo() {
        try {
            System.out.println("🔧 Iniciando entrenamiento del modelo Weka...");

            // Cargar desde classpath
            ClassPathResource resource = new ClassPathResource("farmacia_ventas.arff");
            InputStream inputStream = resource.getInputStream();

            ConverterUtils.DataSource source = new ConverterUtils.DataSource(inputStream);
            Instances data = source.getDataSet();

            System.out.println("📊 Dataset cargado: " + data.numInstances() + " instancias");

            // Configurar atributo clase (último)
            data.setClassIndex(data.numAttributes() - 1);
            System.out.println("🎯 Atributo clase: " + data.classAttribute().name());

            // Entrenar clasificador J48
            classifier = new J48();

            // Configurar opciones seguras
            String[] options = {"-U"}; // Árbol sin podar para más simplicidad
            ((J48) classifier).setOptions(options);

            classifier.buildClassifier(data);

            // Guardar estructura para nuevas instancias
            this.dataStructure = new Instances(data, 0);
            this.modeloTrained = true;

            System.out.println("✅ Modelo entrenado exitosamente");
            System.out.println("📋 Modelo: " + classifier.toString());

        } catch (Exception e) {
            System.err.println("❌ Error entrenando modelo: " + e.getMessage());
            e.printStackTrace();
            // No lanzar excepción, usar predicción por defecto
        }
    }

    /**
     * Predecir demanda para un producto
     */
    public PrediccionDemanda predecirDemanda(Producto producto) {
        if (!modeloTrained) {
            try {
                entrenarModelo();
            } catch (Exception e) {
                System.err.println("⚠️ Usando predicción por defecto");
                return crearPrediccionPorDefecto(producto);
            }
        }

        try {
            // Crear nueva instancia para predicción
            DenseInstance instance = new DenseInstance(dataStructure.numAttributes());
            instance.setDataset(dataStructure);

            // Establecer valores (ajustar índices según tu ARFF)
            instance.setValue(0, producto.getPrecio());        // precio
            instance.setValue(1, producto.getCantidad());      // cantidad en stock
            instance.setValue(2, producto.getPrecio());        // precio_unitario

            // Realizar predicción
            double prediction = classifier.classifyInstance(instance);
            String nivelDemanda = dataStructure.classAttribute().value((int) prediction);

            // Calcular confianza
            double[] distribution = classifier.distributionForInstance(instance);
            double confianza = distribution[(int) prediction] * 100;

            System.out.println("🔮 " + producto.getNombre() + ": " + nivelDemanda +
                    " (" + String.format("%.1f", confianza) + "%)");

            // Crear y guardar predicción
            PrediccionDemanda prediccion = new PrediccionDemanda(
                    producto.getId(),
                    producto.getCodigo(),
                    producto.getNombre(),
                    producto.getPrecio(),
                    producto.getCantidad(),
                    producto.getPrecio(),
                    nivelDemanda,
                    confianza
            );

            return prediccionDemandaRepository.save(prediccion);

        } catch (Exception e) {
            System.err.println("❌ Error en predicción: " + e.getMessage());
            return crearPrediccionPorDefecto(producto);
        }
    }

    /**
     * Predicción por defecto basada en reglas simples
     */
    private PrediccionDemanda crearPrediccionPorDefecto(Producto producto) {
        String nivelDemanda;
        double confianza;

        // Lógica simple basada en stock y precio
        if (producto.getCantidad() < 20 && producto.getPrecio() > 30) {
            nivelDemanda = "alta";
            confianza = 75.0;
        } else if (producto.getCantidad() < 50) {
            nivelDemanda = "media";
            confianza = 65.0;
        } else {
            nivelDemanda = "baja";
            confianza = 70.0;
        }

        return new PrediccionDemanda(
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getCantidad(),
                producto.getPrecio(),
                nivelDemanda,
                confianza
        );
    }

    // Los demás métodos se mantienen igual...
    public List<PrediccionDemandaDTO> predecirDemandaTodosProductos() {
        try {
            List<Producto> productos = productoService.obtenerTodos();
            List<PrediccionDemandaDTO> resultados = new ArrayList<>();

            System.out.println("📦 Procesando " + productos.size() + " productos...");

            for (Producto producto : productos) {
                try {
                    PrediccionDemanda prediccion = predecirDemanda(producto);
                    resultados.add(convertirADTO(prediccion));
                } catch (Exception e) {
                    System.err.println("❌ Error con " + producto.getNombre());
                    // Continuar con siguiente producto
                }
            }

            // Ordenar por prioridad de demanda
            resultados.sort((a, b) -> {
                return Integer.compare(getPriority(b.getNivelDemanda()),
                        getPriority(a.getNivelDemanda()));
            });

            return resultados;

        } catch (Exception e) {
            System.err.println("❌ Error general: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private int getPriority(String nivelDemanda) {
        switch (nivelDemanda.toLowerCase()) {
            case "alta": return 3;
            case "media": return 2;
            case "baja": return 1;
            default: return 0;
        }
    }
        /**
         * Obtener productos con demanda alta (alertas)
         */
        public List<PrediccionDemandaDTO> obtenerProductosDemandaAlta() {
            try {
                List<PrediccionDemanda> predicciones = prediccionDemandaRepository.findByNivelDemandaOrderByConfianzaDesc("alta");
                return predicciones.stream()
                        .map(this::convertirADTO)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                System.err.println("❌ Error obteniendo alertas: " + e.getMessage());
                return new ArrayList<>();
            }
        }

        /**
         * Obtener predicciones recientes
         */
        public List<PrediccionDemandaDTO> obtenerPrediccionesRecientes() {
            try {
                List<PrediccionDemanda> predicciones = prediccionDemandaRepository.findTop10ByOrderByFechaPrediccionDesc();
                return predicciones.stream()
                        .map(this::convertirADTO)
                        .collect(Collectors.toList());
            } catch (Exception e) {
                System.err.println("❌ Error obteniendo predicciones recientes: " + e.getMessage());
                return new ArrayList<>();
            }
        }

        /**
         * Convertir entidad a DTO
         */
        public PrediccionDemandaDTO convertirADTO(PrediccionDemanda prediccion) {
            PrediccionDemandaDTO dto = new PrediccionDemandaDTO();
            dto.setProductoId(prediccion.getProductoId());
            dto.setCodigoProducto(prediccion.getCodigoProducto());
            dto.setNombreProducto(prediccion.getNombreProducto());
            dto.setNivelDemanda(prediccion.getNivelDemanda());
            dto.setConfianza(prediccion.getConfianza());
            dto.setColorAlerta(obtenerColorAlerta(prediccion.getNivelDemanda()));
            return dto;
        }

        /**
         * Determinar color para la UI basado en el nivel de demanda
         */
        private String obtenerColorAlerta(String nivelDemanda) {
            if (nivelDemanda == null) return "secondary";

            switch (nivelDemanda.toLowerCase()) {
                case "alta": return "danger";
                case "media": return "warning";
                case "baja": return "success";
                default: return "secondary";
            }
        }

        /**
         * Re-entrenar modelo
         */
        public boolean reentrenarModelo() {
            try {
                this.classifier = null;
                this.modeloTrained = false;
                entrenarModeloSimple(); // Usar versión simple para evitar errores
                return true;
            } catch (Exception e) {
                System.err.println("❌ Error re-entrenando modelo: " + e.getMessage());
                return false;
            }
        }

        /**
         * Verificar estado del modelo
         */
        public Map<String, Object> obtenerEstadoModelo() {
            Map<String, Object> estado = new HashMap<>();
            estado.put("entrenado", modeloTrained);
            estado.put("clasificador", classifier != null ? classifier.getClass().getSimpleName() : "No inicializado");
            estado.put("atributos", dataStructure != null ? dataStructure.numAttributes() : 0);
            estado.put("archivoCargado", true);

            return estado;
        }

        /**
         * Obtener estadísticas generales
         */
        public Map<String, Object> obtenerEstadisticas() {
            List<PrediccionDemandaDTO> todasPredicciones = predecirDemandaTodosProductos();

            long totalProductos = todasPredicciones.size();
            long demandaAlta = todasPredicciones.stream().filter(p -> "alta".equals(p.getNivelDemanda())).count();
            long demandaMedia = todasPredicciones.stream().filter(p -> "media".equals(p.getNivelDemanda())).count();
            long demandaBaja = todasPredicciones.stream().filter(p -> "baja".equals(p.getNivelDemanda())).count();
            long totalAlertas = demandaAlta;

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalProductos", totalProductos);
            stats.put("demandaAlta", demandaAlta);
            stats.put("demandaMedia", demandaMedia);
            stats.put("demandaBaja", demandaBaja);
            stats.put("totalAlertas", totalAlertas);
            stats.put("porcentajeCritico", totalProductos > 0 ? (demandaAlta * 100.0) / totalProductos : 0);

            return stats;
        }
    public void entrenarModeloSimple() {
        try {
            System.out.println("🔧 Iniciando entrenamiento SIMPLE del modelo Weka...");

            // Cargar el archivo ARFF desde resources
            ClassPathResource resource = new ClassPathResource("farmacia_ventas.arff");
            File arffFile = resource.getFile();

            if (!arffFile.exists()) {
                System.err.println("❌ Archivo ARFF no encontrado");
                throw new RuntimeException("Archivo farmacia_ventas.arff no encontrado");
            }

            // Cargar datos
            ConverterUtils.DataSource source = new ConverterUtils.DataSource(arffFile.getAbsolutePath());
            Instances data = source.getDataSet();

            // Establecer atributo clase
            if (data.classIndex() == -1) {
                data.setClassIndex(data.numAttributes() - 1);
            }

            // Crear y entrenar clasificador SIN opciones
            classifier = new J48();

            System.out.println("🏗️ Entrenando clasificador J48 (sin opciones)...");
            classifier.buildClassifier(data);

            // Guardar estructura de datos
            this.dataStructure = new Instances(data, 0);
            this.modeloTrained = true;

            System.out.println("✅ Modelo entrenado exitosamente (versión simple)");

        } catch (Exception e) {
            System.err.println("❌ Error en entrenamiento simple: " + e.getMessage());
            throw new RuntimeException("Error entrenando modelo simple", e);
        }
    }
}
