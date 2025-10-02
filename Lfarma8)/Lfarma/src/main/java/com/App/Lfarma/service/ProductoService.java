package com.App.Lfarma.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.App.Lfarma.entity.Producto;
import com.App.Lfarma.repository.ProductoRepository;

import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }
    
    public List<Producto> listarProductosOrdenados(Sort sort) {
        return productoRepository.findAll(sort);
    }
    
    public Page<Producto> listarProductosPaginados(String categoria, Pageable pageable) {
        if (categoria != null && !categoria.isEmpty()) {
            return productoRepository.findByCategoria(categoria, pageable);
        }
        return productoRepository.findAll(pageable);
    }
    
    public Page<Producto> buscarProductos(String searchTerm, String categoria, Pageable pageable) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return listarProductosPaginados(categoria, pageable);
        }
        
        if (categoria != null && !categoria.isEmpty()) {
            return productoRepository.searchByCategoryAndTerm(categoria, searchTerm, pageable);
        }
        
        return productoRepository.searchProducts(searchTerm, pageable);
    }

    public Optional<Producto> buscarPorCodigo(String codigo) {
        return productoRepository.findByCodigo(codigo);
    }

    public Optional<Producto> buscarPorId(String id) {
        return productoRepository.findById(id);
    }

    public Producto guardarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    public void eliminarPorId(String id) {
        productoRepository.deleteById(id);
    }
    
    public void descontarStock(String codigo, int cantidad) {
        Optional<Producto> productoOpt = productoRepository.findByCodigo(codigo);
        if (productoOpt.isPresent()) {
            Producto producto = productoOpt.get();
            if (producto.getCantidad() < cantidad) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
            }
            producto.setCantidad(producto.getCantidad() - cantidad);
            productoRepository.save(producto);
        } else {
            throw new NoSuchElementException("No se encontró producto con el código: " + codigo);
        }
    }
}