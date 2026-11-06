package com.example.MedTech.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.MedTech.model.Profissional;

@Repository 
public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

}
