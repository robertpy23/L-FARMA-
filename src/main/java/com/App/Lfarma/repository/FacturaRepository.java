package com.App.Lfarma.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.App.Lfarma.entity.Factura;

@Repository
public interface FacturaRepository extends MongoRepository<Factura, String> {
}