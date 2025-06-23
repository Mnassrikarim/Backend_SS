package com.example.pfe.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categorie")
public class Categorie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titre_categorie", nullable = false)
    private String titreCategorie;

    @Column(name = "description")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @OneToMany(mappedBy = "categorie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SousCategorie> sousCategories = new ArrayList<>();

    // Constructors
    public Categorie() {
    }

    public Categorie(String titreCategorie, String description, String imageUrl) {
        this.titreCategorie = titreCategorie;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitreCategorie() {
        return titreCategorie;
    }

    public void setTitreCategorie(String titreCategorie) {
        this.titreCategorie = titreCategorie;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<SousCategorie> getSousCategories() {
        return sousCategories;
    }

    public void setSousCategories(List<SousCategorie> sousCategories) {
        this.sousCategories = sousCategories;
    }
}