package com.example.pfe.entity.Dto;

import com.example.pfe.entity.Utilisateur.Role;

public class UtilisateurResponseDTO {
    private String nom;
    private String prenom;
    private String email;
    private Role role;



    private String image;
    private String imageUrl;



    // Constructor
    public UtilisateurResponseDTO(String nom, String prenom, String email, Role role, String image, String imageUrl) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
        this.image = image;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }




    public String getImage() {
        return image;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}