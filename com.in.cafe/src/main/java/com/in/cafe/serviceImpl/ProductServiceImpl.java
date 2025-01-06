package com.in.cafe.serviceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.in.cafe.JWT.JwtFilter;
import com.in.cafe.POJO.Category;
import com.in.cafe.POJO.Product;
import com.in.cafe.constents.CafeConstants;
import com.in.cafe.dao.CategoryDao;
import com.in.cafe.dao.ProductDao;
import com.in.cafe.service.ProductService;
import com.in.cafe.utils.CafeUtils;
import com.in.cafe.wrapper.ProductWrapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    ProductDao productDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    JwtFilter jwtFilter;

    @Override
    public ResponseEntity<String> addProduct(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                if (validateProductMap(requestMap, false)) {
                    if (validateCategory(requestMap.get("categoryId"))) {
                        productDao.save(getProductFromMap(requestMap, false));
                        return CafeUtils.getResponseEntity("Product Added Successfully", HttpStatus.OK);
                    } else {
                        return CafeUtils.getResponseEntity("Category doesn't exist", HttpStatus.BAD_REQUEST);
                    }
                } else {
                    return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
                }
            } else {
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateProductMap(Map<String, String> requestMap, boolean validateId) {
        if (requestMap.containsKey("name") && requestMap.containsKey("price") && requestMap.containsKey("description")
                && requestMap.containsKey("categoryId")) {
            if (validateId & requestMap.containsKey("id")) {
                return true;
            } else if (!validateId) {
                return true;
            }
        }
        return false;
    }

    private boolean validateCategory(String categoryId) {
        try {
            Category category = categoryDao.findById(Integer.parseInt(categoryId)).get();
            if (category != null) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private Product getProductFromMap(Map<String, String> requestMap, boolean isUpdate) {
        Category category = new Category();
        category.setId(Integer.parseInt(requestMap.get("categoryId")));

        Product product = new Product();
        if (isUpdate) {
            product.setId(Integer.parseInt(requestMap.get("id")));
        } else {
            product.setStatus("true");
        }
        product.setCategory(category);
        product.setName(requestMap.get("name"));
        product.setDescription(requestMap.get("description"));
        product.setPrice(Integer.parseInt(requestMap.get("price")));
        return product;
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getAllProduct() {
        try {
            return new ResponseEntity<>(productDao.getAllProduct(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<List<ProductWrapper>>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateProduct(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                if (validateProductMap(requestMap, true)) {
                    if (validateCategory(requestMap.get("categoryId"))) {
                        Optional<Product> optional = productDao.findById(Integer.parseInt(requestMap.get("id")));
                        if (optional.isPresent()) {
                            Product product = getProductFromMap(requestMap, true);
                            product.setStatus(optional.get().getStatus());
                            productDao.save(product);
                            return CafeUtils.getResponseEntity("Product Updated Successfully", HttpStatus.OK);
                        } else {
                            return CafeUtils.getResponseEntity("Product Not Found", HttpStatus.NOT_FOUND);
                        }
                    } else {
                        return CafeUtils.getResponseEntity("Category doesn't exist", HttpStatus.BAD_REQUEST);
                    }
                } else {
                    return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
                }
            } else {
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> removeProduct(Integer id) {
        try {
            if (jwtFilter.isAdmin()) {
                Optional<Product> optional = productDao.findById(id);
                if (optional.isPresent()) {
                    // Product product = optional.get();
                    // product.setStatus("false");
                    // productDao.save(product);
                    productDao.deleteById(id);
                    return CafeUtils.getResponseEntity("Product Removed Successfully", HttpStatus.OK);
                } else {
                    return CafeUtils.getResponseEntity("Product Not Found", HttpStatus.NOT_FOUND);
                }
            } else {
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateProductStatus(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()) {
                if (requestMap.containsKey("id") && requestMap.containsKey("status")) {
                    Optional<Product> optional = productDao.findById(Integer.parseInt(requestMap.get("id")));
                    if (optional.isPresent()) {
                        productDao.updateProductStatus(Integer.parseInt(requestMap.get("id")),
                                requestMap.get("status"));
                        return CafeUtils.getResponseEntity("Product Status Updated Successfully", HttpStatus.OK);
                    } else {
                        return CafeUtils.getResponseEntity("Product Not Found", HttpStatus.NOT_FOUND);
                    }
                } else {
                    return CafeUtils.getResponseEntity(CafeConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
                }
            } else {
                return CafeUtils.getResponseEntity(CafeConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<ProductWrapper>> getByCategoryId(Integer categoryId) {
        try {
            return new ResponseEntity<>(productDao.getByCategoryId(categoryId), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<List<ProductWrapper>>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<ProductWrapper> getByProductId(Integer productId) {
        try {
            Optional<Product> optional = productDao.findById(productId);
            if (optional.isPresent()) {
                return new ResponseEntity<>(productDao.getByProductId(productId), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ProductWrapper(), HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(new ProductWrapper(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
