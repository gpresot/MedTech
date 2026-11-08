package com.example.MedTech.exception;

/** Violação de uma regra de negócio (quarto lotado, conflito de horário, etc.). */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
