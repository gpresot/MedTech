package com.example.MedTech.model;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("CLINICO")
public class MedicoClinicoGeral extends Profissional {

    public MedicoClinicoGeral() {
        super();
    }

    public MedicoClinicoGeral( String nome, String cpf,
                              String email, String telefone, String crm) {
        super( nome, cpf, email, telefone, crm);
    }

    public void realizarConsultaRotina() {
        System.out.println("Consulta de rotina realizada.");
    }

    public void realizarAvaliacaoInicial() {
        System.out.println("Avaliação inicial realizada.");
    }

    public void encaminharParaEspecialista() {
        System.out.println("Paciente encaminhado para um especialista.");
    }

    public void registrarConsulta(String observacoes) {
        System.out.println("Consulta registrada: " + observacoes);
    }
}
