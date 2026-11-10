package com.example.MedTech.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.MedTech.model.Cirurgiao;

@Repository
public interface CirurgiaoRepository extends JpaRepository<Cirurgiao, Long> {

    List<Cirurgiao> findAllByOrderByNomeAsc();
}
