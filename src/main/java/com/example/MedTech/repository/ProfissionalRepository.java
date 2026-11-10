
package com.example.MedTech.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.MedTech.model.Profissional;
import com.example.MedTech.enums.Especialidade;

@Repository
public interface ProfissionalRepository extends JpaRepository<Profissional, Long> {

    @Query("SELECT m FROM MedicoEspecialista m WHERE m.especialidade = :especialidade")
    List<Profissional> findByEspecialidade(
            @Param("especialidade") Especialidade especialidade);

    Optional<Profissional> findByEmail(String email);

    boolean existsByCrm(String crm);

    boolean existsByCrmAndIdNot(String crm, Long id);
}
