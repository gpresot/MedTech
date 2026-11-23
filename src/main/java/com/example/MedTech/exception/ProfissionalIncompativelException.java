package com.example.MedTech.exception;

/**
 * O profissional informado não pode responder pelo atendimento pedido
 * (ex.: clínico geral numa cirurgia, especialista de outra área numa consulta
 * especializada).
 *
 * É uma {@link RegraNegocioException}: continua virando HTTP 409 no
 * {@code GlobalExceptionHandler}, mas permite tratar essa falha em separado.
 */
public class ProfissionalIncompativelException extends RegraNegocioException {

    public ProfissionalIncompativelException(String mensagem) {
        super(mensagem);
    }
}
