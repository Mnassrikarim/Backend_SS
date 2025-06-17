package com.example.pfe.services;


import com.example.pfe.entity.Utilisateur;
import com.example.pfe.repositries.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    public List<Utilisateur> getAllUsers() {
        return utilisateurRepository.findAll();
    }

    public Optional<Utilisateur> getUserById(Long id) {
        return utilisateurRepository.findById(id);
    }

    public Utilisateur createUser(Utilisateur utilisateur) {
        return utilisateurRepository.save(utilisateur);
    }

    public void deleteUser(Long id) {
        utilisateurRepository.deleteById(id);
    }

    public Optional<Utilisateur> getUserByEmail(String email) {return utilisateurRepository.findByEmail(email);}
}