package com.App.Lfarma.config;

import com.App.Lfarma.service.PrediccionDemandaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModeloPrediccionConfig {

    @Autowired
    private PrediccionDemandaService prediccionDemandaService;

    @Bean
    public CommandLineRunner inicializarModelo() {
        return args -> {
            try {
                System.out.println("🚀 Inicializando modelo de predicción de demanda...");
                prediccionDemandaService.entrenarModeloSimple(); // Usar versión simple
                System.out.println("✅ Modelo de predicción inicializado correctamente");
            } catch (Exception e) {
                System.err.println("⚠️ El modelo no pudo inicializarse, pero la aplicación continuará: " + e.getMessage());
                // No lanzamos excepción para permitir que la aplicación inicie
            }
        };
    }
}