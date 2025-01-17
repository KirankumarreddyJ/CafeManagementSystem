package com.in.cafe.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import com.in.cafe.POJO.Product;
import com.in.cafe.wrapper.ProductWrapper;

import jakarta.transaction.Transactional;

public interface ProductDao extends JpaRepository<Product, Integer> {

    List<ProductWrapper> getAllProduct();

    @Modifying
    @Transactional
    Integer updateProductStatus(@Param("id") Integer id, @Param("status") String status);

    List<ProductWrapper> getByCategoryId(@Param("categoryId") Integer categoryId);

    ProductWrapper getByProductId(@Param("productId") Integer productId);

}
