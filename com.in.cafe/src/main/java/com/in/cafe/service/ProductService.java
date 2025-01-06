package com.in.cafe.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.in.cafe.wrapper.ProductWrapper;

public interface ProductService {

    ResponseEntity<String> addProduct(Map<String, String> requestMap);

    ResponseEntity<List<ProductWrapper>> getAllProduct();

    ResponseEntity<String> updateProduct(Map<String, String> requestMap);

    ResponseEntity<String> removeProduct(Integer id);

    ResponseEntity<String> updateProductStatus(Map<String, String> requestMap);

    ResponseEntity<List<ProductWrapper>> getByCategoryId(Integer categoryId);

    ResponseEntity<ProductWrapper> getByProductId(Integer productId);

}
