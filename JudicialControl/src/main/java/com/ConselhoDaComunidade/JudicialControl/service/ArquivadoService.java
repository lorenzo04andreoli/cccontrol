package com.ConselhoDaComunidade.JudicialControl.service;

import com.ConselhoDaComunidade.JudicialControl.entity.Arquivado;
import com.ConselhoDaComunidade.JudicialControl.repository.ArquivadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class ArquivadoService {

    @Autowired
    private ArquivadoRepository repo;

    public Page<Arquivado> buscarArquivados(String termo, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nome").ascending());

        boolean hasTermo = termo != null && !termo.isBlank();
        if (!hasTermo) {
            return repo.findAll(pageable);
        }

        String termoTrim = termo.trim();
        String termoCpf = termoTrim.replaceAll("\\D", "");
        boolean pareceCpf = !termoCpf.isBlank() && termoCpf.length() >= 3;

        String termoFinal = pareceCpf ? termoCpf : termoTrim;

        return repo.findByNomeContainingIgnoreCaseOrCpfContainingIgnoreCase(termoFinal, termoFinal, pageable);
    }
}
