package com.example.MedTech.factory;

import com.example.MedTech.dto.ProfissionalDTO;
import com.example.MedTech.model.*;

public class ProfissionalFactory {

    public static Profissional criar(ProfissionalDTO dto) {
        if (dto.getTipo() == null) {
            throw new IllegalArgumentException("O tipo de profissional é obrigatório.");
        }

        if ("ESPECIALISTA".equalsIgnoreCase(dto.getTipo()) && dto.getEspecialidade() == null) {
            throw new IllegalArgumentException("A especialidade é obrigatória para médicos especialistas.");
        }
        
        Profissional profissional;
        switch (dto.getTipo().toUpperCase()) {

            case "CLINICO":
                profissional = new MedicoClinicoGeral();
                break;

            case "ESPECIALISTA":
                profissional = new MedicoEspecialista();
                break;

            case "CIRURGIAO":
                profissional = new Cirurgiao();
                break;

            default:
                throw new IllegalArgumentException("Tipo de profissional inválido.");
        }

        if (profissional instanceof MedicoEspecialista) {
            MedicoEspecialista especialista = (MedicoEspecialista) profissional;
            especialista.setEspecialidade(dto.getEspecialidade());
        }

        profissional.setNome(dto.getNome());
        profissional.setCpf(dto.getCpf());
        profissional.setEmail(dto.getEmail());
        profissional.setTelefone(dto.getTelefone());
        profissional.setCrm(dto.getCrm());

        return profissional;
    }
}
