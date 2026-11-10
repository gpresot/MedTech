package com.example.MedTech.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Guarda as informações relevantes do paciente. Internações e procedimentos
 * são consultados pelos próprios repositórios (ligados ao paciente), assim
 * o histórico nunca fica desatualizado em relação a eles.
 */
@Entity
@Table(name = "historicos_medicos")
public class HistoricoMedico {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false, unique = true)
    private Paciente paciente;

    @Column(columnDefinition = "TEXT")
    private String informacoesRelevantes;

    public HistoricoMedico() {
    }

    public HistoricoMedico(Paciente paciente) {
        this.paciente = paciente;
        this.informacoesRelevantes = "";
    }

    public void adicionarInformacao(String informacao) {
        String registro = "[" + LocalDateTime.now().format(FORMATO) + "] "
                + informacao.trim();

        if (informacoesRelevantes == null || informacoesRelevantes.isBlank()) {
            informacoesRelevantes = registro;
        } else {
            informacoesRelevantes += "\n" + registro;
        }
    }

    public Long getId() { return id; }

    public Paciente getPaciente() { return paciente; }

    public String getInformacoesRelevantes() { return informacoesRelevantes; }
}
