package com.example.MedTech.model;

import java.time.LocalDate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "pacientes")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false)
    private LocalDate dataNascimento;

    private String telefone;

    private String endereco;

    @Column(nullable = false, unique = true)
    private String email;

    // Composição: o histórico nasce e morre junto com o paciente.
    @OneToOne(mappedBy = "paciente", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private HistoricoMedico historicoMedico;

    public Paciente() {
    }

    public Paciente(String nome, String cpf, LocalDate dataNascimento,
            String telefone, String endereco, String email) {
        this.nome = nome;
        this.cpf = cpf;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.endereco = endereco;
        this.email = email;
        this.historicoMedico = new HistoricoMedico(this);
    }

    public void atualizarContato(String telefone, String email) {
        this.telefone = telefone;
        this.email = email;
    }

    public Long getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public HistoricoMedico getHistoricoMedico() { return historicoMedico; }
}
