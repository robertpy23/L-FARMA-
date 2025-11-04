package com.App.Lfarma.repository;

import com.App.Lfarma.entity.PrediccionDemanda;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrediccionDemandaRepository extends MongoRepository<PrediccionDemanda, String> {

    List<PrediccionDemanda> findByNivelDemandaOrderByConfianzaDesc(String nivelDemanda);

    List<PrediccionDemanda> findByProductoIdOrderByFechaPrediccionDesc(String productoId);

    List<PrediccionDemanda> findTop10ByOrderByFechaPrediccionDesc();

    List<PrediccionDemanda> findByNivelDemandaAndConfianzaGreaterThan(String nivelDemanda, double confianza);
}