package com.hotbake.service;

import com.hotbake.dto.ProductDto;
import com.hotbake.enums.ProductCategory;
import com.hotbake.exception.ResourceNotFoundException;
import com.hotbake.exception.UnauthorizedException;
import com.hotbake.model.Product;
import com.hotbake.model.ProductImage;
import com.hotbake.model.SellerProfile;
import com.hotbake.repository.ProductImageRepository;
import com.hotbake.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
public class ProductService {

    @Autowired private ProductRepository productRepository;
    @Autowired private ProductImageRepository productImageRepository;

    public Page<Product> getProducts(int page, int size, String sort, ProductCategory category) {
        Sort sorting = switch (sort != null ? sort : "newest") {
            case "price_asc"  -> Sort.by("pricePerPound").ascending();
            case "price_desc" -> Sort.by("pricePerPound").descending();
            case "rating"     -> Sort.by("createdAt").descending();
            default           -> Sort.by("createdAt").descending();
        };
        Pageable pageable = PageRequest.of(page, size, sorting);
        if (category != null) {
            return productRepository.findByDeletedFalseAndAvailableTrueAndCategory(category, pageable);
        }
        return productRepository.findByDeletedFalseAndAvailableTrue(pageable);
    }

    public Page<Product> searchProducts(String query, ProductCategory category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (category != null) {
            return productRepository.searchProductsWithCategory(query, category, pageable);
        }
        return productRepository.searchProducts(query, pageable);
    }

    public Product findById(Long id) {
        return productRepository.findByIdWithImages(id)
                .filter(p -> !p.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public List<Product> getLatestProducts(int count) {
        return productRepository.findLatestProducts(PageRequest.of(0, count));
    }

    public List<Product> getSellerProducts(SellerProfile sellerProfile) {
        return productRepository.findBySellerProfileAndDeletedFalse(sellerProfile);
    }

    public Product createProduct(ProductDto dto, SellerProfile sellerProfile) throws IOException {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setCategory(dto.getCategory());
        product.setPricePerPound(dto.getPricePerPound());
        product.setAvailable(dto.isAvailable());
        product.setSellerProfile(sellerProfile);
        product = productRepository.save(product);

        if (dto.getImages() != null) {
            saveImages(product, dto.getImages());
        }
        return product;
    }

    public Product updateProduct(Long id, ProductDto dto, SellerProfile sellerProfile) throws IOException {
        Product product = findById(id);
        if (!product.getSellerProfile().getId().equals(sellerProfile.getId())) {
            throw new UnauthorizedException("You don't own this product.");
        }
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setCategory(dto.getCategory());
        product.setPricePerPound(dto.getPricePerPound());
        product.setAvailable(dto.isAvailable());

        if (dto.getImages() != null && !dto.getImages().isEmpty()
                && !dto.getImages().get(0).isEmpty()) {
            product.getImages().clear();
            productRepository.save(product);
            saveImages(product, dto.getImages());
        }
        return productRepository.save(product);
    }

    public void deleteProduct(Long id, SellerProfile sellerProfile) {
        Product product = findById(id);
        if (!product.getSellerProfile().getId().equals(sellerProfile.getId())) {
            throw new UnauthorizedException("You don't own this product.");
        }
        product.setDeleted(true);
        productRepository.save(product);
    }

    public void adminDeleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setDeleted(true);
        productRepository.save(product);
    }

    private void saveImages(Product product, List<MultipartFile> files) throws IOException {
        boolean firstImage = product.getImages().isEmpty();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) continue;
            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setData(file.getBytes());
            String contentType = file.getContentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }
            image.setContentType(contentType);
            image.setOriginalName(file.getOriginalFilename());
            image.setPrimary(firstImage);
            firstImage = false;
            productImageRepository.save(image);
        }
    }

    public ProductImage getImage(Long imageId) {
        return productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));
    }

    public long countAll() {
        return productRepository.findByDeletedFalse().size();
    }

    public List<Product> findAllForAdmin() {
        return productRepository.findByDeletedFalse();
    }
}
