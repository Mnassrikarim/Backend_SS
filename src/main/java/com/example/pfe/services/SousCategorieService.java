package com.example.pfe.services;

import com.example.pfe.entity.SousCategorie;
import com.example.pfe.repositries.SousCategorieRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SousCategorieService {

    @Autowired
    private SousCategorieRepository sousCategorieRepository;

    public SousCategorie createSousCategorie(SousCategorie sousCategorie) {
        return sousCategorieRepository.save(sousCategorie);
    }

    public Optional<SousCategorie> getSousCategorieById(Long id) {
        return sousCategorieRepository.findById(id);
    }
}