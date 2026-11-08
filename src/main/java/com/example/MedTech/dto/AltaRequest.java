package com.example.MedTech.dto;

import java.time.LocalDate;

/** dataAlta é opcional: se vier nula, usa a data de hoje. */
public record AltaRequest(LocalDate dataAlta) {
}
