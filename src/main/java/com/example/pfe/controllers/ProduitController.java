package com.example.pfe.controllers;

import com.example.pfe.entity.Produit;
import com.example.pfe.entity.SousCategorie;
import com.example.pfe.entity.Utilisateur;
import com.example.pfe.entity.Utilisateur.Role;
import com.example.pfe.security.JwtUtil;
import com.example.pfe.services.ProduitService;
import com.example.pfe.services.SousCategorieService;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/produits")
public class ProduitController {

    @Autowired
    private ProduitService produitService;

    @Autowired
    private SousCategorieService sousCategorieService;

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
    public ResponseEntity<?> createProduit(
            @RequestHeader("Authorization") String token,
            @RequestParam("titreProduit") String titreProduit,
            @RequestParam("prix") Double prix,
            @RequestParam("quantity") Integer quantity,
            @RequestParam("description") String description,
            @RequestParam("sousCategorieId") Long sousCategorieId,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        ResponseEntity<?> authCheck = checkAdminAuthorization(token);
        if (authCheck != null) return authCheck;

        try {
            Optional<SousCategorie> sousCategorieOpt = sousCategorieService.getSousCategorieById(sousCategorieId);
            if (sousCategorieOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sous-catégorie introuvable");
            }

            String imageName = null;
            if (image != null && !image.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                Path imagePath = Paths.get(uploadDir + fileName);
                Files.createDirectories(imagePath.getParent());
                Files.write(imagePath, image.getBytes());
                imageName = fileName;
            }

            Produit produit = new Produit();
            produit.setTitreProduit(titreProduit);
            produit.setPrix(prix);
            produit.setQuantity(quantity);
            produit.setDescription(description);
            produit.setImageUrl(imageName);
            produit.setSousCategorie(sousCategorieOpt.get());

            Produit createdProduit = produitService.createProduit(produit);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduit);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la création du produit : " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllProduits(@RequestHeader("Authorization") String token) {
        ResponseEntity<?> authCheck = checkAdminAuthorization(token);
        if (authCheck != null) return authCheck;

        try {
            List<Produit> produits = produitService.getAllProduits();
            return ResponseEntity.ok(produits);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération des produits : " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProduitById(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        ResponseEntity<?> authCheck = checkAdminAuthorization(token);
        if (authCheck != null) return authCheck;

        try {
            Optional<Produit> produitOpt = produitService.getProduitById(id);
            if (produitOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produit introuvable");
            }
            return ResponseEntity.ok(produitOpt.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération du produit : " + e.getMessage());
        }
    }

    @PutMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduit(
            @RequestHeader("Authorization") String token,
            @RequestParam("id") Long id,
            @RequestParam("titreProduit") String titreProduit,
            @RequestParam("prix") Double prix,
            @RequestParam("quantity") Integer quantity,
            @RequestParam("description") String description,
            @RequestParam("sousCategorieId") Long sousCategorieId,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        ResponseEntity<?> authCheck = checkAdminAuthorization(token);
        if (authCheck != null) return authCheck;

        try {
            Optional<Produit> produitOpt = produitService.getProduitById(id);
            if (produitOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produit introuvable");
            }

            Optional<SousCategorie> sousCategorieOpt = sousCategorieService.getSousCategorieById(sousCategorieId);
            if (sousCategorieOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sous-catégorie introuvable");
            }

            Produit produit = produitOpt.get();
            produit.setTitreProduit(titreProduit);
            produit.setPrix(prix);
            produit.setQuantity(quantity);
            produit.setDescription(description);
            produit.setSousCategorie(sousCategorieOpt.get());

            if (image != null && !image.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                Path imagePath = Paths.get(uploadDir + fileName);
                Files.createDirectories(imagePath.getParent());
                Files.write(imagePath, image.getBytes());
                produit.setImageUrl(fileName);
            }

            Produit updatedProduit = produitService.updateProduit(produit);
            return ResponseEntity.ok(updatedProduit);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la mise à jour du produit : " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProduit(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        ResponseEntity<?> authCheck = checkAdminAuthorization(token);
        if (authCheck != null) return authCheck;

        try {
            Optional<Produit> produitOpt = produitService.getProduitById(id);
            if (produitOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Produit introuvable");
            }
            produitService.deleteProduit(id);
            return ResponseEntity.ok("Produit supprimé avec succès");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la suppression du produit : " + e.getMessage());
        }
    }
}