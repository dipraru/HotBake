package com.hotbake.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hotbake.enums.ProductCategory;
import com.hotbake.model.Product;
import com.hotbake.model.SellerProfile;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

  @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.sellerProfile WHERE p.id = :id")
  java.util.Optional<Product> findByIdWithImages(@Param("id") Long id);

      @EntityGraph(attributePaths = {"images", "sellerProfile"})
    Page<Product> findByDeletedFalseAndAvailableTrue(Pageable pageable);

      @EntityGraph(attributePaths = {"images", "sellerProfile"})
    Page<Product> findByDeletedFalseAndAvailableTrueAndCategory(ProductCategory category, Pageable pageable);

      @EntityGraph(attributePaths = {"images", "sellerProfile"})
    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.available = true " +
           "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Product> searchProducts(@Param("query") String query, Pageable pageable);

      @EntityGraph(attributePaths = {"images", "sellerProfile"})
    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.available = true " +
           "AND p.category = :category " +
           "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Product> searchProductsWithCategory(@Param("query") String query,
                                              @Param("category") ProductCategory category,
                                              Pageable pageable);

       @EntityGraph(attributePaths = {"images"})
       List<Product> findBySellerProfileAndDeletedFalse(SellerProfile sellerProfile);

      @EntityGraph(attributePaths = {"images", "sellerProfile"})
       List<Product> findByDeletedFalse();

      @EntityGraph(attributePaths = {"images", "sellerProfile"})
    @Query("SELECT p FROM Product p WHERE p.deleted = false AND p.available = true ORDER BY p.createdAt DESC")
    List<Product> findLatestProducts(Pageable pageable);
}
