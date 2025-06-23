package com.example.pfe.controllers;

import com.example.pfe.entity.Utilisateur;
import com.example.pfe.entity.Utilisateur.Role;
import com.example.pfe.entity.Dto.UtilisateurResponseDTO;
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
import java.util.*;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private EmailService emailService;

    private final String uploadDir = "backend/uploads/images/";

    @PostMapping("/register")
    public ResponseEntity<?> createUser(
            @RequestParam("nom") String nom,
            @RequestParam("prenom") String prenom,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        try {
            Optional<Utilisateur> existingUser = utilisateurService.getUserByEmail(email);
            if (existingUser.isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Un utilisateur avec cet email existe déjà");
            }

            String imageName = null;
            if (image != null && !image.isEmpty()) {
                String originalFileName = image.getOriginalFilename();
                Path imagePath = Paths.get(uploadDir + originalFileName);
                Files.createDirectories(imagePath.getParent());
                Files.write(imagePath, image.getBytes());
                imageName = originalFileName;
            }

            String referralCode = UUID.randomUUID().toString();
            Utilisateur utilisateur = new Utilisateur();
            utilisateur.setNom(nom);
            utilisateur.setPrenom(prenom);
            utilisateur.setEmail(email);
            utilisateur.setPassword(password);
            utilisateur.setRole(Role.USER);
            utilisateur.setImage(imageName);
            utilisateur.setStatus(Utilisateur.Status.PENDING);
            utilisateur.setReferralCode(referralCode);

            Utilisateur newUser = utilisateurService.createUser(utilisateur);
            String verificationLink = "http://localhost:4200/verify?email=" + newUser.getEmail() + "&referralCode=" + referralCode;
            emailService.sendVerificationEmail(newUser.getEmail(), verificationLink);

            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'inscription : " + e.getMessage());
        }
    }

    
    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam("email") String email, @RequestParam("referralCode") String referralCode) {
        Optional<Utilisateur> userOpt = utilisateurService.getUserByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur introuvable.");
        }

        Utilisateur user = userOpt.get();

        if (!user.getReferralCode().equals(referralCode)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Code de vérification invalide.");
        }

        if (Utilisateur.Status.APPROVED.equals(user.getStatus())) {
            return ResponseEntity.ok("Compte déjà approuvé.");
        }

        user.setStatus(Utilisateur.Status.APPROVED);
        utilisateurService.createUser(user);

        return ResponseEntity.ok("Votre compte a été vérifié avec succès !");
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Utilisateur utilisateur) {
        Optional<Utilisateur> optionalUser = utilisateurService.getUserByEmail(utilisateur.getEmail());
        if (optionalUser.isPresent()) {
            Utilisateur user = optionalUser.get();
            if (user.getPassword().equals(utilisateur.getPassword())) {
                if (!Utilisateur.Status.APPROVED.equals(user.getStatus())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                            .body("Votre compte n'est pas encore approuvé. Veuillez attendre la validation.");
                }
                String token = jwtUtil.generateToken(user.getEmail());
                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("user", user);
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou mot de passe incorrect");
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<?> getUserDetails(
            @PathVariable Long id,
            @RequestHeader("Authorization") String tokenHeader
    ) {
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
        }

        String token = tokenHeader.substring(7);
        String email;

        try {
            email = jwtUtil.extractEmail(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        if (!jwtUtil.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token expired or invalid");
        }

        Optional<Utilisateur> userOpt = utilisateurService.getUserById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        Utilisateur user = userOpt.get();

        if (!user.getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only view your own profile");
        }

        String imageUrl = "http://localhost:8081/api/utilisateurs/uploads/images/" + user.getImage();

        UtilisateurResponseDTO responseDTO = new UtilisateurResponseDTO(
                user.getNom(),
                user.getPrenom(),
                user.getEmail(),
                user.getRole(),
                user.getImage(),
                imageUrl
        );

        return ResponseEntity.ok(responseDTO);
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