package com.App.Lfarma.config;

import com.App.Lfarma.service.PrediccionDemandaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModeloPrediccionConfig {

    private static final Logger log = LoggerFactory.getLogger(ModeloPrediccionConfig.class);

    @Autowired
    private PrediccionDemandaService prediccionDemandaService;

    @Bean
    public CommandLineRunner inicializarModelo() {
        return args -> {
            long startTime = System.currentTimeMillis();
            String taskName = "inicialización del modelo de predicción";

            try {
                log.info("🚀 Iniciando {}...", taskName);
                prediccionDemandaService.entrenarModeloSimple();

                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;

                log.info("✅ {} completada exitosamente en {} ms", taskName, duration);

            } catch (Exception e) {
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;

                log.error("⚠️ {} falló después de {} ms, pero la aplicación continuará. Error: {}",
                        taskName, duration, e.getMessage(), e);
                // No lanzamos excepción para permitir que la aplicación inicie
            }
        };
    }
}