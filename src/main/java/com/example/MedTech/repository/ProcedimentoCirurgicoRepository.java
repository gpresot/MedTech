package com.example.MedTech.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.MedTech.enums.StatusAtendimento;
import com.example.MedTech.model.ProcedimentoCirurgico;

@Repository
public interface ProcedimentoCirurgicoRepository
        extends JpaRepository<ProcedimentoCirurgico, Long> {

    List<ProcedimentoCirurgico> findByInternacaoIdOrderByDataHoraDesc(Long internacaoId);

    List<ProcedimentoCirurgico> findByInternacaoPacienteIdOrderByDataHoraDesc(Long pacienteId);

    boolean existsByInternacaoIdAndStatusIn(Long internacaoId,
            Collection<StatusAtendimento> status);

    // Procedimentos ativos do cirurgião que colidem com a janela (inicio, fim).
    @Query("SELECT COUNT(p) FROM ProcedimentoCirurgico p "
            + "WHERE p.cirurgiao.id = :cirurgiaoId AND p.status IN :status "
            + "AND p.dataHora > :inicio AND p.dataHora < :fim")
    long contarConflitosDoCirurgiao(@Param("cirurgiaoId") Long cirurgiaoId,
            @Param("status") Collection<StatusAtendimento> status,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);

    // Idem, para o paciente (não pode estar em duas cirurgias ao mesmo tempo).
    @Query("SELECT COUNT(p) FROM ProcedimentoCirurgico p "
            + "WHERE p.internacao.paciente.id = :pacienteId AND p.status IN :status "
            + "AND p.dataHora > :inicio AND p.dataHora < :fim")
    long contarConflitosDoPaciente(@Param("pacienteId") Long pacienteId,
            @Param("status") Collection<StatusAtendimento> status,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim);
}
