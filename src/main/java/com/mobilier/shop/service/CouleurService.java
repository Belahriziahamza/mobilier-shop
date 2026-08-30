package com.mobilier.shop.service;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

import com.mobilier.shop.entity.Couleur;
import com.mobilier.shop.repository.CouleurRepository;


@Service
public class CouleurService {


    private static final Pattern HEX_PATTERN =
            Pattern.compile(
                    "^#[0-9A-Fa-f]{6}$"
            );


    private final CouleurRepository couleurRepository;



    public CouleurService(
            CouleurRepository couleurRepository
    ) {

        this.couleurRepository =
                couleurRepository;
    }



    /* =====================================================
       LISTES
    ===================================================== */

    public List<Couleur> findAll() {

        return couleurRepository
                .findAllByOrderByCreatedAtDesc();
    }


    public List<Couleur> findActive() {

        return couleurRepository
                .findByActiveTrueOrderByNameAsc();
    }



    /* =====================================================
       COMPTEURS
    ===================================================== */

    public long countAll() {

        return couleurRepository.count();
    }


    public long countActive() {

        return couleurRepository
                .countByActiveTrue();
    }



    /* =====================================================
       FIND
    ===================================================== */

    public Couleur findById(Long id) {

        return couleurRepository
                .findById(id)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "Couleur introuvable."
                                )
                );
    }



    /* =====================================================
       CREATION
    ===================================================== */

    public Couleur create(
            Couleur form
    ) {

        validate(
                form,
                null
        );


        Couleur couleur =
                new Couleur();


        copyFields(
                couleur,
                form
        );


        return couleurRepository.save(
                couleur
        );

    }



    /* =====================================================
       MODIFICATION
    ===================================================== */

    public Couleur update(

            Long id,

            Couleur form
    ) {

        Couleur couleur =
                findById(id);


        validate(
                form,
                id
        );


        copyFields(
                couleur,
                form
        );


        return couleurRepository.save(
                couleur
        );

    }



    /* =====================================================
       STATUT
    ===================================================== */

    public Couleur toggleStatus(
            Long id
    ) {

        Couleur couleur =
                findById(id);


        couleur.setActive(
                !couleur.isActive()
        );


        return couleurRepository.save(
                couleur
        );

    }



    /* =====================================================
       DELETE
    ===================================================== */

    public void delete(Long id) {

        Couleur couleur =
                findById(id);


        /*
         * La protection contre une couleur utilisée
         * par un produit sera ajoutée avec la relation
         * Product <-> Couleur à l'étape suivante.
         */

        couleurRepository.delete(
                couleur
        );

    }



    /* =====================================================
       VALIDATION
    ===================================================== */

    private void validate(

            Couleur form,

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
                    "Le nom de la couleur est obligatoire."
            );

        }


        if (
                form.getCodeHex() == null
                ||
                !HEX_PATTERN
                        .matcher(
                                form.getCodeHex().trim()
                        )
                        .matches()
        ) {

            throw new IllegalArgumentException(
                    "Code couleur invalide. Exemple : #D8C3A5"
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
                        couleurRepository
                                .existsByReferenceIgnoreCase(
                                        reference
                                );

            } else {

                exists =
                        couleurRepository
                                .existsByReferenceIgnoreCaseAndIdNot(
                                        reference,
                                        currentId
                                );

            }


            if (exists) {

                throw new IllegalArgumentException(
                        "Cette référence de couleur existe déjà."
                );

            }

        }

    }



    /* =====================================================
       COPY
    ===================================================== */

    private void copyFields(

            Couleur target,

            Couleur source
    ) {

        target.setName(
                cleanRequired(
                        source.getName()
                )
        );


        target.setCodeHex(
                source.getCodeHex()
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        )
        );


        target.setReference(
                normalizeReference(
                        source.getReference()
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
       CLEAN
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