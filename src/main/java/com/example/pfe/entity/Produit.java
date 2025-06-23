package com.example.pfe.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "produit")
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titre_produit", nullable = false)
    private String titreProduit;

    @Column(name = "prix", nullable = false)
    private Double prix;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "etat", nullable = false)
    private Etat etat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sous_categorie_id", nullable = false)
    private SousCategorie sousCategorie;

    // Enum for Etat
    public enum Etat {
        DISPO,
        NOT_DISPO,
        LIMITED
    }

    // Constructors
    public Produit() {
    }

    public Produit(String titreProduit, Double prix, Integer quantity, String imageUrl, String description, SousCategorie sousCategorie) {
        this.titreProduit = titreProduit;
        this.prix = prix;
        this.quantity = quantity;
        this.imageUrl = imageUrl;
        this.description = description;
        this.sousCategorie = sousCategorie;
        updateEtat();
    }

    // Update Etat based on quantity
    public void updateEtat() {
        if (quantity == 0) {
            this.etat = Etat.NOT_DISPO;
        } else if (quantity >= 1 && quantity <= 5) {
            this.etat = Etat.LIMITED;
        } else {
            this.etat = Etat.DISPO;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitreProduit() {
        return titreProduit;
    }

    public void setTitreProduit(String titreProduit) {
        this.titreProduit = titreProduit;
    }

    public Double getPrix() {
        return prix;
    }

    public void setPrix(Double prix) {
        this.prix = prix;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        updateEtat();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Etat getEtat() {
        return etat;
    }

    public void setEtat(Etat etat) {
        this.etat = etat;
    }

    public SousCategorie getSousCategorie() {
        return sousCategorie;
    }

    public void setSousCategorie(SousCategorie sousCategorie) {
        this.sousCategorie = sousCategorie;
    }
}