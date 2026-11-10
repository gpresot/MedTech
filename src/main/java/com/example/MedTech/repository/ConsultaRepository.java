package com.example.MedTech.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.MedTech.enums.StatusConsulta;
import com.example.MedTech.model.Consulta;
import com.example.MedTech.model.Profissional;

import jakarta.persistence.LockModeType;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    List<Consulta> findByPacienteIdOrderByDataDescHorarioDesc(Long pacienteId);

    List<Consulta> findByStatus(StatusConsulta status);

    boolean existsByProfissionalId(Long profissionalId);

    @Query("SELECT COUNT(c) FROM Consulta c WHERE c.profissional.id = :profissionalId "
            + "AND c.data = :data AND c.horario = :horario AND c.status <> :cancelada "
            + "AND (:consultaId IS NULL OR c.id <> :consultaId)")
    long contarConflitos(@Param("profissionalId") Long profissionalId,
            @Param("data") LocalDate data,
            @Param("horario") LocalTime horario,
            @Param("cancelada") StatusConsulta cancelada,
            @Param("consultaId") Long consultaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Profissional p WHERE p.id = :id")
    Optional<Profissional> buscarProfissionalParaAgendar(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Consulta c WHERE c.id = :id")
    Optional<Consulta> buscarParaAtualizar(@Param("id") Long id);
}
