package com.example.MedTech.dto;

import com.example.MedTech.model.Profissional;

/** Dados mínimos de um profissional, usados nas listas de seleção das telas. */
public record ProfissionalResumoResponse(Long id, String nome, String crm) {

    public static ProfissionalResumoResponse de(Profissional p) {
        return new ProfissionalResumoResponse(p.getId(), p.getNome(), p.getCrm());
    }
}
