
package com.ConselhoDaComunidade.JudicialControl.controller;

import com.ConselhoDaComunidade.JudicialControl.entity.Reeducando;
import com.ConselhoDaComunidade.JudicialControl.repository.ReeducandoRepository;
import com.ConselhoDaComunidade.JudicialControl.service.ReeducandoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;


import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/reeducandos")
public class ReeducandoController {

    @Autowired
    private ReeducandoService service;

    private final ReeducandoRepository repository;

    @Autowired
    public ReeducandoController(ReeducandoRepository repository) {
        this.repository = repository;
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER','VIEWER')")
    @GetMapping
    public List<Reeducando> listarTodos(){
        return service.listarTodosOrdenados();
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER','VIEWER')")
    @GetMapping("/atrasados")
    public List<Reeducando> listarAtrasados(){
        return service.listarAtrasados();
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PutMapping("/{id}/status")
    public ResponseEntity<?> atualizarStatus(@PathVariable Long id, @RequestBody Map<String, String> payload){
        String status = payload.get("status");
        service.atualizarStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/status")
    public String atualizarStatusViaForm(@PathVariable Long id, @RequestParam String status) {
        service.atualizarStatus(id, status);
        return "redirect:/";
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER','VIEWER')")
    @GetMapping("/{id}")
    public ResponseEntity<Reeducando> buscarPorId(@PathVariable Long id) {
        Optional<Reeducando> reeducando = repository.findById(id);
        return reeducando.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(@PathVariable Long id, @RequestBody Map<String, String> dados) {
        service.atualizarDados(id, dados);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping
    public ResponseEntity<?> adicionar(@RequestBody Reeducando novo) {
        if (novo.getNome() == null || novo.getNome().isBlank()){
            return ResponseEntity.badRequest().body("Nome é obrigatório");
        }

        if (novo.getCpf() == null || novo.getCpf().isBlank()){
            return ResponseEntity.badRequest().body("CPF é obrigatório");
        }

        String cpf = novo.getCpf().replaceAll("\\D", "");
        if (repository.existsByCpf(cpf)) {
            return ResponseEntity.badRequest().body("Já existe um reeducando com este CPF.");
        }
        novo.setCpf(cpf);

        if (novo.getAutos() != null) {
            novo.setAutos(novo.getAutos().trim());
        }

        if (novo.getDia() != null && novo.getFrequencia() != null) {
            novo.setStatus(service.calcularStatus(novo));
        } else {
            novo.setStatus("Em dia");
        }


        repository.save(novo);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @PostMapping("/{id}/arquivar")
    public ResponseEntity<?> arquivar(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String obs = payload.get("observacao");
        try {
            service.arquivar(id, obs);
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }


}
