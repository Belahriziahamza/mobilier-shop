package com.mobilier.shop.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mobilier.shop.entity.Product;
import com.mobilier.shop.repository.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    private final Path uploadRoot =
            Paths.get(
                    "uploads",
                    "products"
            )
            .toAbsolutePath()
            .normalize();


    public ProductService(
            ProductRepository productRepository
    ) {

        this.productRepository =
                productRepository;
    }


    public List<Product> findAll() {

        return productRepository.findAll();
    }


    public List<Product> findActive() {

        return productRepository
                .findByActiveTrueOrderByCreatedAtDesc();
    }


    public List<Product> findByCategory(
            String category
    ) {

        return productRepository
                .findByCategoryAndActiveTrueOrderByCreatedAtDesc(
                        category
                );
    }
public List<Product> findByCategories(
        List<String> categories
) {

    return productRepository
            .findByCategoryInAndActiveTrueOrderByCreatedAtDesc(
                    categories
            );
}

    public List<Product> findNewProducts() {

        return productRepository
                .findByNewProductTrueAndActiveTrueOrderByCreatedAtDesc();
    }


    public List<Product> findPromotions() {

        return productRepository
                .findByPromotionTrueAndActiveTrueOrderByCreatedAtDesc();
    }


    public List<Product> findFeatured() {

        return productRepository
                .findByFeaturedTrueAndActiveTrueOrderByCreatedAtDesc();
    }


    public Product findById(Long id) {

        return productRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Produit introuvable avec l'id : "
                                                + id
                                )
                );
    }


    public Product create(
            Product product,
            MultipartFile image
    ) throws IOException {

        if (
                image == null ||
                image.isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "La photo du produit est obligatoire."
            );
        }


        String imagePath =
                saveImage(image);


        product.setId(null);

        product.setImagePath(
                imagePath
        );

        product.setActive(true);


        return productRepository.save(
                product
        );
    }


    public Product update(
            Long id,
            Product formProduct,
            MultipartFile image
    ) throws IOException {

        Product product =
                findById(id);


        product.setName(
                formProduct.getName()
        );

        product.setCategory(
                formProduct.getCategory()
        );

        product.setType(
                formProduct.getType()
        );

        product.setPrice(
                formProduct.getPrice()
        );

        product.setOldPrice(
                formProduct.getOldPrice()
        );

        product.setColor(
                formProduct.getColor()
        );

        product.setAvailability(
                formProduct.getAvailability()
        );

        product.setDescription(
                formProduct.getDescription()
        );

        product.setSourceUrl(
                formProduct.getSourceUrl()
        );

        product.setActive(
                formProduct.isActive()
        );

        product.setFeatured(
                formProduct.isFeatured()
        );

        product.setNewProduct(
                formProduct.isNewProduct()
        );

        product.setPromotion(
                formProduct.isPromotion()
        );


        if (
                image != null &&
                !image.isEmpty()
        ) {

            String oldImage =
                    product.getImagePath();


            String newImage =
                    saveImage(image);


            product.setImagePath(
                    newImage
            );


            deleteImageFile(
                    oldImage
            );
        }


        return productRepository.save(
                product
        );
    }


    public void delete(Long id)
            throws IOException {

        Product product =
                findById(id);


        String imagePath =
                product.getImagePath();


        productRepository.delete(
                product
        );


        deleteImageFile(
                imagePath
        );
    }


    private String saveImage(
            MultipartFile image
    ) throws IOException {

        if (
                image.getSize() >
                10L * 1024L * 1024L
        ) {

            throw new IllegalArgumentException(
                    "L'image ne doit pas dépasser 10 MB."
            );
        }


        String contentType =
                image.getContentType();


        if (contentType == null) {

            throw new IllegalArgumentException(
                    "Type de fichier invalide."
            );
        }


        String extension = switch (contentType) {

            case "image/jpeg" -> ".jpg";

            case "image/png" -> ".png";

            case "image/webp" -> ".webp";

            default -> throw new IllegalArgumentException(
                    "Formats autorisés : JPG, PNG et WebP."
            );
        };


        Files.createDirectories(
                uploadRoot
        );


        String fileName =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        + extension;


        Path destination =
                uploadRoot
                        .resolve(fileName)
                        .normalize();


        if (
                !destination.startsWith(
                        uploadRoot
                )
        ) {

            throw new IllegalArgumentException(
                    "Chemin de fichier invalide."
            );
        }


        Files.copy(
                image.getInputStream(),
                destination,
                StandardCopyOption.REPLACE_EXISTING
        );


        return "/uploads/products/"
                + fileName;
    }


    private void deleteImageFile(
            String imagePath
    ) throws IOException {

        if (
                imagePath == null ||
                imagePath.isBlank()
        ) {

            return;
        }


        String prefix =
                "/uploads/products/";


        if (
                !imagePath.startsWith(
                        prefix
                )
        ) {

            return;
        }


        String fileName =
                imagePath.substring(
                        prefix.length()
                );


        Path file =
                uploadRoot
                        .resolve(fileName)
                        .normalize();


        if (
                file.startsWith(
                        uploadRoot
                )
        ) {

            Files.deleteIfExists(
                    file
            );
        }
    }
}