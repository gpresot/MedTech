package com.example.MedTech.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.MedTech.model.Quarto;

import jakarta.persistence.LockModeType;

@Repository
public interface QuartoRepository extends JpaRepository<Quarto, Long> {

    boolean existsByNumero(int numero);

    boolean existsByNumeroAndIdNot(int numero, Long id);

    @Query("SELECT q FROM Quarto q "
            + "WHERE q.status <> com.example.MedTech.enums.StatusQuarto.MANUTENCAO "
            + "AND q.ocupacaoAtual < q.capacidadeMaxima ORDER BY q.andar, q.numero")
    List<Quarto> findDisponiveis();

    // Trava a linha para evitar duas internações disputando a última vaga.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM Quarto q WHERE q.id = :id")
    Optional<Quarto> findByIdParaAtualizar(@Param("id") Long id);
}
