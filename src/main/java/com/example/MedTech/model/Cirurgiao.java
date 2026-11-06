
package com.example.MedTech.model;

import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("CIRURGIAO")
public class Cirurgiao extends Profissional {

    public Cirurgiao() {
        super();
    }

    public Cirurgiao( String nome, String cpf,
                     String email, String telefone, String crm) {

        super( nome, cpf, email, telefone, crm);
    }

    public void realizarCirurgia() {
        System.out.println("Procedimento cirúrgico realizado.");
    }

    public void registrarProcedimento(
            LocalDate data,
            String nomePaciente,
            String descricao) {

        System.out.println("Procedimento registrado.");
        System.out.println("Data: " + data);
        System.out.println("Paciente: " + nomePaciente);
        System.out.println("Descrição: " + descricao);
    }

    public void registrarObservacoesPreOperatorias(String observacoes) {
        System.out.println("Observações pré-operatórias: " + observacoes);
    }

    public void registrarObservacoesPosOperatorias(String observacoes) {
        System.out.println("Observações pós-operatórias: " + observacoes);
    }
}
