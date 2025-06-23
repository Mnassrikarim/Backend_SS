package com.example.pfe.repositries;

import com.example.pfe.entity.SousCategorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SousCategorieRepository extends JpaRepository<SousCategorie, Long> {
}