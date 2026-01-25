package com.ConselhoDaComunidade.JudicialControl.controller;

import com.ConselhoDaComunidade.JudicialControl.entity.Arquivado;
import com.ConselhoDaComunidade.JudicialControl.repository.ArquivadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/arquivados")
public class ArquivadoController {

    @Autowired
    private ArquivadoRepository repo;

    @PreAuthorize("hasAnyRole('ADMIN','USER','VIEWER')")
    @GetMapping
    public List<Arquivado> listar() {
        return repo.findAll(Sort.by(Sort.Direction.ASC, "nome"));
    }
}
