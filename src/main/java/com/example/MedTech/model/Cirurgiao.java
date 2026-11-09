
package com.example.MedTech.model;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("CIRURGIAO")
public class Cirurgiao extends Profissional {

    public Cirurgiao() {
        super();
    }

    public Cirurgiao(String nome, String cpf,
                     String email, String telefone, String crm) {

        super(nome, cpf, email, telefone, crm);
    }
}
