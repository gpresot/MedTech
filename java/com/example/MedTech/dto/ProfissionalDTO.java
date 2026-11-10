package com.example.MedTech.dto;

import com.example.MedTech.enums.Especialidade;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ProfissionalDTO {
    @NotBlank(message = "O tipo de profissional é obrigatório.")
    private String tipo;
    @NotBlank(message = "O nome é obrigatório.")
    private String nome;
    @NotBlank(message = "O CPF é obrigatório.")
    @Pattern(regexp = "\\d{11}", message = "O CPF deve conter exatamente 11 números.")
    private String cpf;
    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Informe um e-mail válido.")
    private String email;
    @NotBlank(message = "O telefone é obrigatório.")
    @Pattern(regexp = "\\d{10,11}", message = "O telefone deve conter 10 ou 11 números.")
    private String telefone;
    @NotBlank(message = "O CRM é obrigatório.")
    private String crm;
    private Especialidade especialidade;

    public ProfissionalDTO() {
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getCrm() {
        return crm;
    }

    public void setCrm(String crm) {
        this.crm = crm;
    }

    public Especialidade getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(Especialidade especialidade) {
        this.especialidade = especialidade;
    }

}
