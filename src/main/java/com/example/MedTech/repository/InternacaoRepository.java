package com.example.MedTech.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.MedTech.model.Internacao;

@Repository
public interface InternacaoRepository extends JpaRepository<Internacao, Long> {

    boolean existsByPacienteId(Long pacienteId);

    boolean existsByQuartoId(Long quartoId);

    boolean existsByProfissionalId(Long profissionalId);

    boolean existsByPacienteIdAndDataEfetivaAltaIsNull(Long pacienteId);

    List<Internacao> findByDataEfetivaAltaIsNullOrderByDataEntradaDesc();

    List<Internacao> findByPacienteIdOrderByDataEntradaDesc(Long pacienteId);
}
