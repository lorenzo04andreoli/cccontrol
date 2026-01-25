package com.ConselhoDaComunidade.JudicialControl.DTO;

import java.time.LocalDate;

public record ProximoCumprimentoDto(
        Long id,
        String nome,
        String cpf,
        String telefone,
        String frequencia,
        LocalDate ultimoComparecimento,
        LocalDate proximoComparecimento,
        long diasRestantes
) {}
