package com.mobilier.shop.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mobilier.shop.entity.Tissu;
import com.mobilier.shop.repository.TissuRepository;


@Service
public class TissuService {


    private static final long MAX_IMAGE_SIZE =
            5L * 1024L * 1024L;


    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(
                    "jpg",
                    "jpeg",
                    "png",
                    "webp"
            );


    private final TissuRepository tissuRepository;


    private final Path tissuUploadPath;



    public TissuService(

            TissuRepository tissuRepository,

            @Value("${app.upload-dir:uploads}")
            String uploadDir
    ) {

        this.tissuRepository =
                tissuRepository;


        this.tissuUploadPath =
                Path.of(
                        uploadDir,
                        "tissus"
                )
                .toAbsolutePath()
                .normalize();

    }



    /* =====================================================
       LISTES
    ===================================================== */

    public List<Tissu> findAll() {

        return tissuRepository
                .findAllByOrderByCreatedAtDesc();
    }


    public List<Tissu> findActive() {

        return tissuRepository
                .findByActiveTrueOrderByNameAsc();
    }



    /* =====================================================
       COMPTEURS
    ===================================================== */

    public long countAll() {

        return tissuRepository.count();
    }


    public long countActive() {

        return tissuRepository
                .countByActiveTrue();
    }



    /* =====================================================
       FIND
    ===================================================== */

    public Tissu findById(Long id) {

        return tissuRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Tissu introuvable."
                                )
                );
    }



    /* =====================================================
       CREATION
    ===================================================== */

    public Tissu create(

            Tissu form,

            MultipartFile image
    ) {

        validate(
                form,
                null
        );


        Tissu tissu =
                new Tissu();


        copyFields(
                tissu,
                form
        );


        if (
                image != null
                &&
                !image.isEmpty()
        ) {

            tissu.setImagePath(
                    saveImage(image)
            );

        }


        return tissuRepository.save(
                tissu
        );

    }



    /* =====================================================
       MODIFICATION
    ===================================================== */

    public Tissu update(

            Long id,

            Tissu form,

            MultipartFile image
    ) {

        Tissu tissu =
                findById(id);


        validate(
                form,
                id
        );


        copyFields(
                tissu,
                form
        );


        if (
                image != null
                &&
                !image.isEmpty()
        ) {

            tissu.setImagePath(
                    saveImage(image)
            );

        }


        return tissuRepository.save(
                tissu
        );

    }



    /* =====================================================
       STATUT
    ===================================================== */

    public Tissu toggleStatus(Long id) {

        Tissu tissu =
                findById(id);


        tissu.setActive(
                !tissu.isActive()
        );


        return tissuRepository.save(
                tissu
        );

    }



    /* =====================================================
       SUPPRESSION
    ===================================================== */

    public void delete(Long id) {

        Tissu tissu =
                findById(id);


        /*
         * Pour l'instant aucune relation Product <-> Tissu
         * n'existe encore.
         *
         * Lors de l'étape 2, on empêchera automatiquement
         * la suppression si le tissu est utilisé.
         */

        tissuRepository.delete(
                tissu
        );

    }



    /* =====================================================
       VALIDATION
    ===================================================== */

    private void validate(

            Tissu form,

            Long currentId
    ) {


        if (
                form == null
                ||
                form.getName() == null
                ||
                form.getName().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Le nom du tissu est obligatoire."
            );

        }


        String reference =
                cleanOptional(
                        form.getReference()
                );


        if (reference != null) {


            boolean exists;


            if (currentId == null) {

                exists =
                        tissuRepository
                                .existsByReferenceIgnoreCase(
                                        reference
                                );

            } else {

                exists =
                        tissuRepository
                                .existsByReferenceIgnoreCaseAndIdNot(
                                        reference,
                                        currentId
                                );

            }


            if (exists) {

                throw new IllegalArgumentException(
                        "Cette référence de tissu existe déjà."
                );

            }

        }

    }



    /* =====================================================
       COPIER CHAMPS
    ===================================================== */

    private void copyFields(

            Tissu target,

            Tissu source
    ) {

        target.setName(
                cleanRequired(
                        source.getName()
                )
        );


        target.setReference(
                normalizeReference(
                        source.getReference()
                )
        );


        target.setType(
                cleanOptional(
                        source.getType()
                )
        );


        target.setDescription(
                cleanOptional(
                        source.getDescription()
                )
        );


        target.setAvailable(
                source.isAvailable()
        );


        target.setActive(
                source.isActive()
        );

    }



    /* =====================================================
       IMAGE
    ===================================================== */

    private String saveImage(
            MultipartFile image
    ) {


        validateImage(
                image
        );


        String originalName =
                image.getOriginalFilename();


        String extension =
                getExtension(
                        originalName
                );


        String fileName =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        +
                        "."
                        +
                        extension;


        try {


            Files.createDirectories(
                    tissuUploadPath
            );


            Path destination =
                    tissuUploadPath
                            .resolve(fileName)
                            .normalize();


            Files.copy(
                    image.getInputStream(),
                    destination,
                    StandardCopyOption.REPLACE_EXISTING
            );


            return "/uploads/tissus/"
                    +
                    fileName;


        } catch (IOException e) {


            throw new IllegalArgumentException(
                    "Impossible d'enregistrer l'image du tissu."
            );

        }

    }



    private void validateImage(
            MultipartFile image
    ) {


        if (
                image.getSize()
                >
                MAX_IMAGE_SIZE
        ) {

            throw new IllegalArgumentException(
                    "L'image du tissu ne doit pas dépasser 5 Mo."
            );

        }


        String extension =
                getExtension(
                        image.getOriginalFilename()
                );


        if (
                !ALLOWED_EXTENSIONS
                        .contains(extension)
        ) {

            throw new IllegalArgumentException(
                    "Format image non autorisé. Utilisez JPG, JPEG, PNG ou WEBP."
            );

        }

    }



    private String getExtension(
            String fileName
    ) {


        if (
                fileName == null
                ||
                !fileName.contains(".")
        ) {

            throw new IllegalArgumentException(
                    "Extension de l'image invalide."
            );

        }


        return fileName
                .substring(
                        fileName.lastIndexOf(".") + 1
                )
                .toLowerCase(
                        Locale.ROOT
                );

    }



    /* =====================================================
       NETTOYAGE
    ===================================================== */

    private String cleanRequired(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }


    private String cleanOptional(
            String value
    ) {

        if (
                value == null
                ||
                value.isBlank()
        ) {

            return null;
        }


        return value.trim();
    }


    private String normalizeReference(
            String value
    ) {

        String clean =
                cleanOptional(
                        value
                );


        if (clean == null) {
            return null;
        }


        return clean.toUpperCase(
                Locale.ROOT
        );

    }

}