package com.example.MedTech.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.MedTech.model.Paciente;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByEmail(String email);

    boolean existsByCpf(String cpf);

    boolean existsByEmail(String email);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);
}
