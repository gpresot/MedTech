package com.example.MedTech.config;

import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Textos de tela (com acento) para os status. Usado nos templates:
 * ${@rotulos.de(c.status())}. Mantém a mesma lista do ROTULOS em static/js/api.js.
 */
@Component("rotulos")
public class Rotulos {

    private static final Map<String, String> TEXTOS = Map.ofEntries(
            Map.entry("AGENDADA", "Agendada"),
            Map.entry("CONFIRMADA", "Confirmada"),
            Map.entry("EM_ATENDIMENTO", "Em atendimento"),
            Map.entry("CONCLUIDA", "Concluída"),
            Map.entry("CANCELADA", "Cancelada"),
            Map.entry("NAO_COMPARECEU", "Não compareceu"),
            Map.entry("AGENDADO", "Agendado"),
            Map.entry("EM_ANDAMENTO", "Em andamento"),
            Map.entry("CONCLUIDO", "Concluído"),
            Map.entry("CANCELADO", "Cancelado"),
            Map.entry("ELETIVA", "Eletiva"),
            Map.entry("URGENCIA", "Urgência"),
            Map.entry("EMERGENCIA", "Emergência"),
            Map.entry("ENFERMARIA", "Enfermaria"),
            Map.entry("APARTAMENTO", "Apartamento"),
            Map.entry("UTI", "UTI"));

    public String de(Enum<?> valor) {
        if (valor == null) {
            return "—";
        }

        String texto = TEXTOS.get(valor.name());

        if (texto != null) {
            return texto;
        }

        String generico = valor.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(generico.charAt(0)) + generico.substring(1);
    }
}
