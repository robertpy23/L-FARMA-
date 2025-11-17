package com.App.Lfarma.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.App.Lfarma.entity.Factura;
import java.util.Date;
import java.util.List;

@Repository
public interface FacturaRepository extends MongoRepository<Factura, String> {
    List<Factura> findByFechaBetween(Date desde, Date hasta);
    List<Factura> findByVendedor(String vendedor);
    org.springframework.data.domain.Page<Factura> findByVendedor(String vendedor, org.springframework.data.domain.Pageable pageable);
}
