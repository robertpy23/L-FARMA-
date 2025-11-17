package com.App.Lfarma.repository;

import com.App.Lfarma.entity.Suministro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface SuministroRepository extends MongoRepository<Suministro, String> {

    Page<Suministro> findByProveedorCodigo(String codigoProveedor, Pageable pageable);
    List<Suministro> findByFechaSuministroBetween(Date desde, Date hasta);
    Page<Suministro> findByEstado(String estado, Pageable pageable);

    @Query("{ 'fechaSuministro': { '$gte': ?0, '$lte': ?1 } }")
    Page<Suministro> buscarPorRangoFechas(Date desde, Date hasta, Pageable pageable);
}