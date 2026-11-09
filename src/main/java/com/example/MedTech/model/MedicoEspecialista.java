
package com.example.MedTech.model;

import com.example.MedTech.enums.Especialidade;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@DiscriminatorValue("ESPECIALISTA")
public class MedicoEspecialista extends Profissional {

    @Enumerated(EnumType.STRING)
    private Especialidade especialidade;

    public MedicoEspecialista() {
        super();
    }

    public MedicoEspecialista(String nome, String cpf,
                              String email, String telefone, String crm,
                              Especialidade especialidade) {

        super(nome, cpf, email, telefone, crm);
        this.especialidade = especialidade;
    }

    public Especialidade getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(Especialidade especialidade) {
        this.especialidade = especialidade;
    }
}
