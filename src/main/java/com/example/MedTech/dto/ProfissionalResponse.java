package com.example.MedTech.dto;

import org.hibernate.Hibernate;

import com.example.MedTech.enums.Especialidade;
import com.example.MedTech.model.Cirurgiao;
import com.example.MedTech.model.MedicoClinicoGeral;
import com.example.MedTech.model.MedicoEspecialista;
import com.example.MedTech.model.Profissional;

/** Resposta da API de profissionais: inclui a categoria (CLINICO, ESPECIALISTA, CIRURGIAO). */
public record ProfissionalResponse(
        Long id,
        String tipo,
        String nome,
        String cpf,
        String email,
        String telefone,
        String crm,
        Especialidade especialidade) {

    public static ProfissionalResponse de(Profissional profissional) {
        Object real = Hibernate.unproxy(profissional);
        String tipo = "PROFISSIONAL";
        Especialidade especialidade = null;
        if (real instanceof MedicoEspecialista especialista) {
            tipo = "ESPECIALISTA";
            especialidade = especialista.getEspecialidade();
        } else if (real instanceof MedicoClinicoGeral) {
            tipo = "CLINICO";
        } else if (real instanceof Cirurgiao) {
            tipo = "CIRURGIAO";
        }
        Profissional p = (Profissional) real;
        return new ProfissionalResponse(p.getId(), tipo, p.getNome(), p.getCpf(),
                p.getEmail(), p.getTelefone(), p.getCrm(), especialidade);
    }
}
