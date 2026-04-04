package com.hotbake.service;

import com.hotbake.enums.ProductCategory;
import com.hotbake.exception.ResourceNotFoundException;
import com.hotbake.exception.UnauthorizedException;
import com.hotbake.model.Product;
import com.hotbake.model.SellerProfile;
import com.hotbake.repository.ProductImageRepository;
import com.hotbake.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductImageRepository productImageRepository;
    @InjectMocks private ProductService productService;

    private SellerProfile sellerProfile;
    private Product product;

    @BeforeEach
    void setUp() {
        sellerProfile = new SellerProfile();
        sellerProfile.setId(1L);

        product = new Product();
        product.setId(1L);
        product.setName("Test Cake");
        product.setDescription("Delicious test cake");
        product.setCategory(ProductCategory.CHOCOLATE_CAKE);
        product.setPricePerPound(new BigDecimal("350.00"));
        product.setSellerProfile(sellerProfile);
        product.setDeleted(false);
    }

    @Test
    void findById_existingProduct_returnsProduct() {
        when(productRepository.findByIdWithImages(1L)).thenReturn(Optional.of(product));

        Product result = productService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Cake");
    }

    @Test
    void findById_deletedProduct_throwsNotFound() {
        product.setDeleted(true);
        when(productRepository.findByIdWithImages(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findById_nonExistingProduct_throwsNotFound() {
        when(productRepository.findByIdWithImages(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deleteProduct_withWrongSeller_throwsUnauthorized() {
        SellerProfile otherSeller = new SellerProfile();
        otherSeller.setId(99L);

        when(productRepository.findByIdWithImages(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.deleteProduct(1L, otherSeller))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void adminDeleteProduct_softDeletesProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.adminDeleteProduct(1L);

        assertThat(product.isDeleted()).isTrue();
        verify(productRepository).save(product);
    }
}
