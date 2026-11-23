package com.example.MedTech.unit;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import com.example.MedTech.enums.Especialidade;
import com.example.MedTech.exception.ProfissionalIncompativelException;
import com.example.MedTech.exception.RegraNegocioException;
import com.example.MedTech.model.Cirurgiao;
import com.example.MedTech.model.ConsultaEspecializada;
import com.example.MedTech.model.ConsultaRotina;
import com.example.MedTech.model.MedicoClinicoGeral;
import com.example.MedTech.model.MedicoEspecialista;
import com.example.MedTech.model.Paciente;
import com.example.MedTech.model.Profissional;
import com.example.MedTech.validation.CompatibilidadeProfissional;

import static org.junit.jupiter.api.Assertions.*;

/** Regras de compatibilidade entre tipo de profissional e tipo de atendimento (sem Spring). */
class CompatibilidadeProfissionalTest {

    private final Profissional clinico = new MedicoClinicoGeral("Dr. Clínico", "11111111111",
            "clinico@teste.com", "31911111111", "CRM1");
    private final Profissional cardiologista = new MedicoEspecialista("Dra. Cardio", "22222222222",
            "cardio@teste.com", "31922222222", "CRM2", Especialidade.CARDIOLOGIA);
    private final Profissional cirurgiao = new Cirurgiao("Dr. Cirurgião", "33333333333",
            "cirurgiao@teste.com", "31933333333", "CRM3");

    private final Paciente paciente = new Paciente("Maria", "44444444444",
            LocalDate.of(1990, 1, 1), "31944444444", "Rua A", "maria@teste.com");

    // ---------- clínico geral ----------

    @Test
    void clinicoGeralPodeFazerConsultaDeRotina() {
        assertSame(clinico, CompatibilidadeProfissional.exigirClinicoGeral(clinico));
    }

    @Test
    void especialistaCirurgiaoENuloNaoFazemRotina() {
        assertThrows(ProfissionalIncompativelException.class,
                () -> CompatibilidadeProfissional.exigirClinicoGeral(cardiologista));
        assertThrows(ProfissionalIncompativelException.class,
                () -> CompatibilidadeProfissional.exigirClinicoGeral(cirurgiao));
        assertThrows(ProfissionalIncompativelException.class,
                () -> CompatibilidadeProfissional.exigirClinicoGeral(null));
    }

    // ---------- especialista ----------

    @Test
    void especialistaDaAreaCertaPodeFazerConsultaEspecializada() {
        assertSame(cardiologista,
                CompatibilidadeProfissional.exigirEspecialista(cardiologista, Especialidade.CARDIOLOGIA));
    }

    @Test
    void especialistaDeOutraAreaEPessoalDeOutroTipoSaoRecusados() {
        var outraArea = assertThrows(ProfissionalIncompativelException.class,
                () -> CompatibilidadeProfissional.exigirEspecialista(cardiologista, Especialidade.NEUROLOGIA));
        assertTrue(outraArea.getMessage().contains("especialidade"));

        assertThrows(ProfissionalIncompativelException.class,
                () -> CompatibilidadeProfissional.exigirEspecialista(clinico, Especialidade.CARDIOLOGIA));
        assertThrows(ProfissionalIncompativelException.class,
                () -> CompatibilidadeProfissional.exigirEspecialista(cirurgiao, Especialidade.CARDIOLOGIA));
    }

    // ---------- cirurgião ----------

    @Test
    void soCirurgiaoResponsavelPorCirurgia() {
        assertSame(cirurgiao, CompatibilidadeProfissional.exigirCirurgiao(cirurgiao));

        assertThrows(ProfissionalIncompativelException.class,
                () -> CompatibilidadeProfissional.exigirCirurgiao(clinico));
        assertThrows(ProfissionalIncompativelException.class,
                () -> CompatibilidadeProfissional.exigirCirurgiao(cardiologista));
    }

    // ---------- a exceção ----------

    @Test
    void incompatibilidadeContinuaSendoRegraDeNegocio() {
        // Mantém o tratamento atual (HTTP 409) no GlobalExceptionHandler.
        assertInstanceOf(RegraNegocioException.class, new ProfissionalIncompativelException("x"));
    }

    // ---------- as consultas usam a mesma validação ----------

    @Test
    void consultaDeRotinaRecusaEspecialista() {
        var consulta = new ConsultaRotina(paciente, cardiologista, LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), "Revisão", null);

        assertThrows(ProfissionalIncompativelException.class, consulta::validarAtendimento);
    }

    @Test
    void consultaDeRotinaAceitaClinico() {
        var consulta = new ConsultaRotina(paciente, clinico, LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), "Revisão", null);

        assertDoesNotThrow(consulta::validarAtendimento);
    }

    @Test
    void consultaEspecializadaExigeAEspecialidadeDoMedico() {
        var certa = new ConsultaEspecializada(paciente, cardiologista, LocalDate.now().plusDays(1),
                LocalTime.of(9, 0), "Coração", null, Especialidade.CARDIOLOGIA);
        var errada = new ConsultaEspecializada(paciente, cardiologista, LocalDate.now().plusDays(1),
                LocalTime.of(10, 0), "Cabeça", null, Especialidade.NEUROLOGIA);

        assertDoesNotThrow(certa::validarAtendimento);
        assertThrows(ProfissionalIncompativelException.class, errada::validarAtendimento);
    }
}
