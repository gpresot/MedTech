
package com.example.MedTech.model;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("CLINICO")
public class MedicoClinicoGeral extends Profissional {

    public MedicoClinicoGeral() {
        super();
    }

    public MedicoClinicoGeral(String nome, String cpf,
                              String email, String telefone, String crm) {
        super(nome, cpf, email, telefone, crm);
    }
}
