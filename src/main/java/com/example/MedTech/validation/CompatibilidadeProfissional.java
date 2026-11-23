package com.example.MedTech.validation;

import org.hibernate.Hibernate;

import com.example.MedTech.enums.Especialidade;
import com.example.MedTech.exception.ProfissionalIncompativelException;
import com.example.MedTech.model.Cirurgiao;
import com.example.MedTech.model.MedicoClinicoGeral;
import com.example.MedTech.model.MedicoEspecialista;
import com.example.MedTech.model.Profissional;

/**
 * Ponto único da regra "este profissional pode responder por este atendimento?".
 * Consultas (rotina e especializada) e cirurgias chamam estes métodos em vez de
 * repetir {@code instanceof} e mensagens cada uma por conta própria.
 *
 * Todos fazem {@link Hibernate#unproxy(Object)}: o Hibernate pode entregar um
 * proxy da classe base {@link Profissional}, e {@code instanceof} falharia nele.
 */
public final class CompatibilidadeProfissional {

    private CompatibilidadeProfissional() {
    }

    /** Consulta de rotina: só clínico geral. */
    public static MedicoClinicoGeral exigirClinicoGeral(Profissional profissional) {
        if (desproxiar(profissional) instanceof MedicoClinicoGeral clinico) {
            return clinico;
        }
        throw new ProfissionalIncompativelException(
                "Consultas de rotina exigem um médico clínico geral.");
    }

    /** Consulta especializada: especialista, e da especialidade pedida. */
    public static MedicoEspecialista exigirEspecialista(Profissional profissional,
            Especialidade especialidade) {

        if (!(desproxiar(profissional) instanceof MedicoEspecialista especialista)) {
            throw new ProfissionalIncompativelException(
                    "A consulta especializada exige um médico especialista.");
        }
        if (especialista.getEspecialidade() != especialidade) {
            throw new ProfissionalIncompativelException(
                    "A especialidade do médico não corresponde à consulta.");
        }
        return especialista;
    }

    /** Cirurgia: só cirurgião. */
    public static Cirurgiao exigirCirurgiao(Profissional profissional) {
        if (desproxiar(profissional) instanceof Cirurgiao cirurgiao) {
            return cirurgiao;
        }
        throw new ProfissionalIncompativelException(
                "Apenas profissionais do tipo Cirurgião podem ser responsáveis por cirurgias.");
    }

    private static Object desproxiar(Profissional profissional) {
        return profissional == null ? null : Hibernate.unproxy(profissional);
    }
}
