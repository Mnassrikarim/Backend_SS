package com.example.pfe.controllers;

import com.example.pfe.entity.Utilisateur;
import com.example.pfe.entity.Utilisateur.Role;
import com.example.pfe.security.JwtUtil;
import com.example.pfe.services.EmailService;
import com.example.pfe.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
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
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    private final String uploadDir = "backend/uploads/images/";

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsersForAdmin(@RequestHeader("Authorization") String token) {
        try {
            String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            Optional<Utilisateur> utilisateurOpt = utilisateurService.getUserByEmail(email);
            if (utilisateurOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur introuvable");
            }

            Utilisateur utilisateur = utilisateurOpt.get();
            if (utilisateur.getRole() != Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accès refusé : vous devez être administrateur.");
            }

            List<Utilisateur> utilisateurs = utilisateurService.getAllUsers();
            return ResponseEntity.ok(utilisateurs);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération des utilisateurs : " + e.getMessage());
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        try {
            String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            Optional<Utilisateur> utilisateurOpt = utilisateurService.getUserByEmail(email);
            if (utilisateurOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur introuvable");
            }

            Utilisateur utilisateur = utilisateurOpt.get();
            if (utilisateur.getRole() != Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accès refusé : vous devez être administrateur.");
            }

            Optional<Utilisateur> user = utilisateurService.getUserById(id);
            if (user.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur introuvable");
            }
            return ResponseEntity.ok(user.get());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la récupération de l'utilisateur : " + e.getMessage());
        }
    }

    @PutMapping("/users/update")
    public ResponseEntity<?> updateUser(
            @RequestHeader("Authorization") String token,
            @RequestParam("nom") String nom,
            @RequestParam("prenom") String prenom,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        try {
            String extractedEmail = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            Optional<Utilisateur> optionalUser = utilisateurService.getUserByEmail(extractedEmail);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur introuvable");
            }

            Utilisateur existingUser = optionalUser.get();
            if (existingUser.getRole() != Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accès refusé : vous devez être administrateur.");
            }

            if (!existingUser.getEmail().equals(email)) {
                Optional<Utilisateur> userWithEmail = utilisateurService.getUserByEmail(email);
                if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(existingUser.getId())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("Cet email est déjà utilisé par un autre utilisateur");
                }
            }

            existingUser.setNom(nom);
            existingUser.setPrenom(prenom);
            existingUser.setEmail(email);
            existingUser.setPassword(password);

            if (image != null && !image.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
                Path imagePath = Paths.get(uploadDir + fileName);
                Files.createDirectories(imagePath.getParent());
                Files.write(imagePath, image.getBytes());
                existingUser.setImage(fileName);
            }

            Utilisateur updatedUser = utilisateurService.createUser(existingUser);
            return ResponseEntity.ok(updatedUser);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    @DeleteMapping("/users/delete/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        try {
            String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            Optional<Utilisateur> utilisateurOpt = utilisateurService.getUserByEmail(email);
            if (utilisateurOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur introuvable");
            }

            Utilisateur utilisateur = utilisateurOpt.get();
            if (utilisateur.getRole() != Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Accès refusé : vous devez être administrateur.");
            }

            Optional<Utilisateur> optionalUser = utilisateurService.getUserById(id);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur introuvable");
            }
            utilisateurService.deleteUser(id);
            return ResponseEntity.ok("Utilisateur supprimé avec succès");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la suppression : " + e.getMessage());
        }
    }


    @GetMapping("/uploads/images/{filename}")
    public ResponseEntity<FileSystemResource> getImage(@PathVariable String filename) {
        try {
            Path path = Paths.get(uploadDir + filename);
            FileSystemResource resource = new FileSystemResource(path.toFile());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(getMediaType(filename))
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private MediaType getMediaType(String filename) {
        if (filename.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        } else if (filename.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        } else {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}