package com.hotbake.controller;

import com.hotbake.model.ProductImage;
import com.hotbake.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ImageController {

    @Autowired
    private ProductService productService;

    @GetMapping("/images/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        try {
            ProductImage image = productService.getImage(id);
            if (image.getData() == null || image.getData().length == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
            if (image.getContentType() != null && !image.getContentType().isBlank()) {
                try {
                    contentType = MediaType.parseMediaType(image.getContentType());
                } catch (Exception ignored) {
                    contentType = MediaType.APPLICATION_OCTET_STREAM;
                }
            }

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .body(image.getData());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
