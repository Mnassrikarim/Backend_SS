package com.example.pfe.controllers;

import com.example.pfe.entity.Categorie;
import com.example.pfe.entity.Utilisateur;
import com.example.pfe.entity.Utilisateur.Role;
import com.example.pfe.security.JwtUtil;
import com.example.pfe.services.CategorieService;
import com.example.pfe.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/categories")
public class CategorieController {

    @Autowired
    private CategorieService categorieService;

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private JwtUtil jwtUtil;

    private final String uploadDir = "backend/uploads/images/";

    private ResponseEntity<?> checkAdminAuthorization(String token) {
        try {
            String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            Optional<Utilisateur> utilisateurOpt = utilisateurService.getUserByEmail(email);
            if (utilisateurOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur introuvable");
            }
            if (utilisateurOpt.get().getRole() != Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accès refusé : vous devez être administrateur.");
            }
            return null;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token invalide ou expiré");
        }
    }

    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createCategorie(
            @RequestHeader("Authorization") String token,
            @RequestParam("titreCategorie") String titreCategorie,
            @RequestParam("description") String description,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        ResponseEntity<?> authCheck = checkAdminAuthorization(token);
        if (authCheck != null) return authCheck;

        try {
            String imageName = null;
            if (image != null && !image.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                Path imagePath = Paths.get(uploadDir + fileName);
                Files.createDirectories(imagePath.getParent());
                Files.write(imagePath, image.getBytes());
                imageName = fileName;
            }

            Categorie categorie = new Categorie(titreCategorie, description, imageName);
            Categorie createdCategorie = categorieService.createCategorie(categorie);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCategorie);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la création de la catégorie : " + e.getMessage());
        }
    }
}