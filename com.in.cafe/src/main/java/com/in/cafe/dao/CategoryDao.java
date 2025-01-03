package com.in.cafe.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.in.cafe.POJO.Category;

public interface CategoryDao extends JpaRepository<Category, Integer> {

    List<Category> getAllCategory();

    List<Category> findByName(String name);
}
