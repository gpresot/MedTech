package com.example.MedTech.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.MedTech.model.HistoricoMedico;

@Repository
public interface HistoricoMedicoRepository extends JpaRepository<HistoricoMedico, Long> {

    Optional<HistoricoMedico> findByPacienteId(Long pacienteId);
}
