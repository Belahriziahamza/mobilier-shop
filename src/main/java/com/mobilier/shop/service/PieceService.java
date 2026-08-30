package com.mobilier.shop.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mobilier.shop.entity.Piece;
import com.mobilier.shop.repository.PieceRepository;


@Service
public class PieceService {


    /* =========================================================
       CATALOGUES AUTORISES
    ========================================================= */

    public static final String CATALOGUE_TAPISSERIE =
            "TAPISSERIE";

    public static final String CATALOGUE_COUTURE =
            "COUTURE";

    public static final String CATALOGUE_MATIERES_TISSUS =
            "MATIERES_TISSUS";



    /* =========================================================
       CONFIGURATION
    ========================================================= */

    private static final long MAX_IMAGE_SIZE =
            5L * 1024L * 1024L;


    private final PieceRepository pieceRepository;

    private final Path uploadDirectory;



    /* =========================================================
       CONSTRUCTEUR
    ========================================================= */

    public PieceService(

            PieceRepository pieceRepository,

            @Value("${app.upload-dir:uploads}")
            String uploadDir
    ) {

        this.pieceRepository =
                pieceRepository;


        this.uploadDirectory =
                Paths.get(uploadDir)
                        .toAbsolutePath()
                        .normalize()
                        .resolve("pieces");


        try {

            Files.createDirectories(
                    this.uploadDirectory
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Impossible de créer le dossier des images des pièces.",
                    e
            );
        }
    }



    /* =========================================================
       TOUTES LES PIECES
    ========================================================= */

    public List<Piece> findAll() {

        return pieceRepository
                .findAll()
                .stream()
                .sorted(
                        (a, b) -> {

                            if (
                                    a.getCreatedAt() == null
                                    &&
                                    b.getCreatedAt() == null
                            ) {

                                return 0;
                            }


                            if (a.getCreatedAt() == null) {

                                return 1;
                            }


                            if (b.getCreatedAt() == null) {

                                return -1;
                            }


                            return b.getCreatedAt()
                                    .compareTo(
                                            a.getCreatedAt()
                                    );
                        }
                )
                .toList();
    }



    /* =========================================================
       TROUVER PAR ID
    ========================================================= */

    public Piece findById(
            Long id
    ) {

        if (id == null) {

            throw new IllegalArgumentException(
                    "Identifiant de la pièce invalide."
            );
        }


        return pieceRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Pièce introuvable."
                                )
                );
    }



    /* =========================================================
       PIECES PAR CATALOGUE
    ========================================================= */

    public List<Piece> findByCatalogue(
            String catalogue
    ) {

        String normalizedCatalogue =
                normalizeCatalogue(
                        catalogue
                );


        return pieceRepository
                .findByCatalogueOrderByCreatedAtDesc(
                        normalizedCatalogue
                );
    }



    /* =========================================================
       PIECES DISPONIBLES PAR CATALOGUE
    ========================================================= */

    public List<Piece> findAvailableByCatalogue(
            String catalogue
    ) {

        String normalizedCatalogue =
                normalizeCatalogue(
                        catalogue
                );


        return pieceRepository
                .findByCatalogueAndAvailableTrueOrderByCreatedAtDesc(
                        normalizedCatalogue
                );
    }



    /* =========================================================
       TOUTES LES PIECES DISPONIBLES
    ========================================================= */

    public List<Piece> findAvailable() {

        return pieceRepository
                .findByAvailableTrueOrderByCreatedAtDesc();
    }



    /* =========================================================
       RECHERCHE ADMIN
    ========================================================= */

    public List<Piece> search(

            String search,

            String catalogue,

            String availability
    ) {


        String normalizedSearch =
                search == null
                        ? ""
                        : search
                                .trim()
                                .toLowerCase(
                                        Locale.ROOT
                                );


        String normalizedCatalogue =
                catalogue == null
                        ? ""
                        : catalogue
                                .trim()
                                .toUpperCase(
                                        Locale.ROOT
                                );


        String normalizedAvailability =
                availability == null
                        ? ""
                        : availability
                                .trim()
                                .toUpperCase(
                                        Locale.ROOT
                                );


        return findAll()
                .stream()


                /* =============================================
                   RECHERCHE NOM / TYPE / DESCRIPTION
                ============================================== */

                .filter(
                        piece -> {


                            if (
                                    normalizedSearch.isBlank()
                            ) {

                                return true;
                            }


                            String name =
                                    safe(
                                            piece.getName()
                                    );


                            String type =
                                    safe(
                                            piece.getType()
                                    );


                            String description =
                                    safe(
                                            piece.getDescription()
                                    );


                            String id =
                                    piece.getId() == null
                                            ? ""
                                            : String.valueOf(
                                                    piece.getId()
                                            );


                            return
                                    name.contains(
                                            normalizedSearch
                                    )
                                    ||
                                    type.contains(
                                            normalizedSearch
                                    )
                                    ||
                                    description.contains(
                                            normalizedSearch
                                    )
                                    ||
                                    id.contains(
                                            normalizedSearch
                                    );
                        }
                )


                /* =============================================
                   FILTRE CATALOGUE
                ============================================== */

                .filter(
                        piece -> {


                            if (
                                    normalizedCatalogue.isBlank()
                            ) {

                                return true;
                            }


                            return normalizedCatalogue.equals(
                                    piece.getCatalogue()
                            );
                        }
                )


                /* =============================================
                   FILTRE DISPONIBILITE
                ============================================== */

                .filter(
                        piece -> {


                            if (
                                    normalizedAvailability.isBlank()
                            ) {

                                return true;
                            }


                            if (
                                    "AVAILABLE".equals(
                                            normalizedAvailability
                                    )
                                    ||
                                    "DISPONIBLE".equals(
                                            normalizedAvailability
                                    )
                            ) {

                                return piece.isAvailable();
                            }


                            if (
                                    "UNAVAILABLE".equals(
                                            normalizedAvailability
                                    )
                                    ||
                                    "INDISPONIBLE".equals(
                                            normalizedAvailability
                                    )
                            ) {

                                return !piece.isAvailable();
                            }


                            return true;
                        }
                )


                .toList();
    }



    /* =========================================================
       AJOUTER UNE PIECE
    ========================================================= */

    public Piece create(

            Piece piece,

            MultipartFile image
    ) {


        if (piece == null) {

            throw new IllegalArgumentException(
                    "Les informations de la pièce sont obligatoires."
            );
        }


        preparePiece(
                piece
        );


        if (
                image != null
                &&
                !image.isEmpty()
        ) {

            String imagePath =
                    saveImage(
                            image
                    );


            piece.setImagePath(
                    imagePath
            );
        }


        return pieceRepository
                .save(
                        piece
                );
    }



    /* =========================================================
       MODIFIER UNE PIECE
    ========================================================= */

    public Piece update(

            Long id,

            Piece formPiece,

            MultipartFile image
    ) {


        Piece existingPiece =
                findById(
                        id
                );


        if (formPiece == null) {

            throw new IllegalArgumentException(
                    "Les informations de la pièce sont obligatoires."
            );
        }



        /* =====================================================
           DONNEES
        ===================================================== */

        existingPiece.setCatalogue(
                normalizeCatalogue(
                        formPiece.getCatalogue()
                )
        );


        existingPiece.setName(
                cleanRequired(
                        formPiece.getName(),
                        "Le nom de la pièce est obligatoire."
                )
        );


        existingPiece.setType(
                cleanRequired(
                        formPiece.getType(),
                        "Le type de la pièce est obligatoire."
                )
        );


        if (
                formPiece.getPrice() == null
        ) {

            throw new IllegalArgumentException(
                    "Le prix est obligatoire."
            );
        }


        if (
                formPiece.getPrice().signum() < 0
        ) {

            throw new IllegalArgumentException(
                    "Le prix ne peut pas être négatif."
            );
        }


        existingPiece.setPrice(
                formPiece.getPrice()
        );


        existingPiece.setDescription(
                cleanOptional(
                        formPiece.getDescription()
                )
        );


        existingPiece.setAvailable(
                formPiece.isAvailable()
        );



        /* =====================================================
           NOUVELLE PHOTO
        ===================================================== */

        if (
                image != null
                &&
                !image.isEmpty()
        ) {


            String oldImagePath =
                    existingPiece.getImagePath();


            String newImagePath =
                    saveImage(
                            image
                    );


            existingPiece.setImagePath(
                    newImagePath
            );


            deleteImageQuietly(
                    oldImagePath
            );
        }



        return pieceRepository
                .save(
                        existingPiece
                );
    }



    /* =========================================================
       DISPONIBLE / NON DISPONIBLE
    ========================================================= */

    public Piece toggleAvailability(
            Long id
    ) {


        Piece piece =
                findById(
                        id
                );


        piece.setAvailable(
                !piece.isAvailable()
        );


        return pieceRepository
                .save(
                        piece
                );
    }



    /* =========================================================
       SUPPRIMER
    ========================================================= */

    public void delete(
            Long id
    ) {


        Piece piece =
                findById(
                        id
                );


        String imagePath =
                piece.getImagePath();


        pieceRepository.delete(
                piece
        );


        deleteImageQuietly(
                imagePath
        );
    }



    /* =========================================================
       STATISTIQUES
    ========================================================= */

    public long countAll() {

        return pieceRepository.count();
    }


    public long countAvailable() {

        return pieceRepository
                .countByAvailableTrue();
    }


    public long countByCatalogue(
            String catalogue
    ) {

        return pieceRepository
                .countByCatalogue(
                        normalizeCatalogue(
                                catalogue
                        )
                );
    }


    public long countAvailableByCatalogue(
            String catalogue
    ) {

        return pieceRepository
                .countByCatalogueAndAvailableTrue(
                        normalizeCatalogue(
                                catalogue
                        )
                );
    }



    /* =========================================================
       PREPARER DONNEES
    ========================================================= */

    private void preparePiece(
            Piece piece
    ) {


        piece.setCatalogue(
                normalizeCatalogue(
                        piece.getCatalogue()
                )
        );


        piece.setName(
                cleanRequired(
                        piece.getName(),
                        "Le nom de la pièce est obligatoire."
                )
        );


        piece.setType(
                cleanRequired(
                        piece.getType(),
                        "Le type de la pièce est obligatoire."
                )
        );


        if (
                piece.getPrice() == null
        ) {

            throw new IllegalArgumentException(
                    "Le prix est obligatoire."
            );
        }


        if (
                piece.getPrice().signum() < 0
        ) {

            throw new IllegalArgumentException(
                    "Le prix ne peut pas être négatif."
            );
        }


        piece.setDescription(
                cleanOptional(
                        piece.getDescription()
                )
        );
    }



    /* =========================================================
       NORMALISER CATALOGUE
    ========================================================= */

    private String normalizeCatalogue(
            String catalogue
    ) {


        if (
                catalogue == null
                ||
                catalogue.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Le catalogue est obligatoire."
            );
        }


        String normalized =
                catalogue
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        )
                        .replace(
                                "-",
                                "_"
                        )
                        .replace(
                                " ",
                                "_"
                        );



        if (
                !CATALOGUE_TAPISSERIE.equals(
                        normalized
                )
                &&
                !CATALOGUE_COUTURE.equals(
                        normalized
                )
                &&
                !CATALOGUE_MATIERES_TISSUS.equals(
                        normalized
                )
        ) {

            throw new IllegalArgumentException(
                    "Catalogue invalide."
            );
        }


        return normalized;
    }



    /* =========================================================
       ENREGISTRER IMAGE
    ========================================================= */

    private String saveImage(
            MultipartFile image
    ) {


        validateImage(
                image
        );


        String originalFilename =
                image.getOriginalFilename();


        String extension =
                getExtension(
                        originalFilename
                );


        String filename =
                UUID.randomUUID()
                        .toString()
                +
                "."
                +
                extension;


        Path target =
                uploadDirectory
                        .resolve(
                                filename
                        )
                        .normalize();


        if (
                !target.startsWith(
                        uploadDirectory
                )
        ) {

            throw new IllegalArgumentException(
                    "Nom de fichier invalide."
            );
        }


        try {


            Files.copy(
                    image.getInputStream(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );


        } catch (IOException e) {


            throw new IllegalStateException(
                    "Impossible d'enregistrer la photo de la pièce.",
                    e
            );
        }


        /*
         * Chemin web enregistré dans MySQL.
         *
         * Exemple :
         * uploads/pieces/xxxx.webp
         */

        return "uploads/pieces/"
                +
                filename;
    }



    /* =========================================================
       VALIDATION IMAGE
    ========================================================= */

    private void validateImage(
            MultipartFile image
    ) {


        if (
                image == null
                ||
                image.isEmpty()
        ) {

            return;
        }


        if (
                image.getSize()
                >
                MAX_IMAGE_SIZE
        ) {

            throw new IllegalArgumentException(
                    "La photo ne doit pas dépasser 5 MB."
            );
        }


        String extension =
                getExtension(
                        image.getOriginalFilename()
                );


        boolean validExtension =
                "jpg".equals(extension)
                ||
                "jpeg".equals(extension)
                ||
                "png".equals(extension)
                ||
                "webp".equals(extension);


        if (!validExtension) {

            throw new IllegalArgumentException(
                    "Format d'image invalide. Utilisez JPG, JPEG, PNG ou WEBP."
            );
        }
    }



    /* =========================================================
       EXTENSION
    ========================================================= */

    private String getExtension(
            String filename
    ) {


        if (
                filename == null
                ||
                filename.isBlank()
                ||
                !filename.contains(".")
        ) {

            throw new IllegalArgumentException(
                    "Le fichier image doit avoir une extension."
            );
        }


        return filename
                .substring(
                        filename.lastIndexOf(".") + 1
                )
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }



    /* =========================================================
       SUPPRIMER ANCIENNE IMAGE
    ========================================================= */

    private void deleteImageQuietly(
            String imagePath
    ) {


        if (
                imagePath == null
                ||
                imagePath.isBlank()
        ) {

            return;
        }


        String filename =
                Paths.get(
                        imagePath.replace("\\", "/")
                )
                .getFileName()
                .toString();


        Path file =
                uploadDirectory
                        .resolve(
                                filename
                        )
                        .normalize();


        if (
                !file.startsWith(
                        uploadDirectory
                )
        ) {

            return;
        }


        try {

            Files.deleteIfExists(
                    file
            );

        } catch (IOException ignored) {

            /*
             * La suppression d'une image ne doit pas
             * empêcher la modification/suppression
             * de la pièce.
             */

        }
    }



    /* =========================================================
       STRING OBLIGATOIRE
    ========================================================= */

    private String cleanRequired(

            String value,

            String message
    ) {


        if (
                value == null
                ||
                value.isBlank()
        ) {

            throw new IllegalArgumentException(
                    message
            );
        }


        return value.trim();
    }



    /* =========================================================
       STRING OPTIONNELLE
    ========================================================= */

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



    /* =========================================================
       STRING SECURISEE POUR RECHERCHE
    ========================================================= */

    private String safe(
            String value
    ) {


        if (value == null) {

            return "";
        }


        return value
                .trim()
                .toLowerCase(
                        Locale.ROOT
                );
    }

}