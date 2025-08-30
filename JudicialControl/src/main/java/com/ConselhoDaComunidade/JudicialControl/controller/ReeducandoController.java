package com.ConselhoDaComunidade.JudicialControl.controller;

import com.ConselhoDaComunidade.JudicialControl.entity.Reeducando;
import com.ConselhoDaComunidade.JudicialControl.repository.ReeducandoRepository;
import com.ConselhoDaComunidade.JudicialControl.service.ReeducandoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public List<Reeducando> listarTodos(){
        return service.listarTodosOrdenados();
    }

    @GetMapping("/atrasados")
    public List<Reeducando> listarAtrasados(){
        return service.listarAtrasados();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> atualizarStatus(@PathVariable Long id, @RequestBody Map<String, String> payload){
        String status = payload.get("status");
        service.atualizarStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reeducandos/{id}/status")
    public String atualizarStatusViaForm(@PathVariable Long id, @RequestParam String status) {
        service.atualizarStatus(id, status);
        return "redirect:/";
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reeducando> buscarPorId(@PathVariable Long id) {
        Optional<Reeducando> reeducando = repository.findById(id);
        return reeducando.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(@PathVariable Long id, @RequestBody Map<String, String> dados) {
        service.atualizarDados(id, dados);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<?> adicionar(@RequestBody Reeducando novo) {
        if (novo.getNome() == null || novo.getNome().isBlank()){
            return ResponseEntity.badRequest().body("Nome é obrigatório");
        }

        if (novo.getCpf() == null || novo.getCpf().isBlank()){
            return ResponseEntity.badRequest().body("CPF é obrigatório");
        }

        if (repository.findAll().stream().anyMatch(r -> r.getCpf().equals(novo.getCpf()))){
            return ResponseEntity.badRequest().body("Já existe um reeducando com este CPF.");
        }

        if (novo.getDia() != null && novo.getFrequencia() != null) {
            boolean atrasado = service.estaAtrasado(novo);
            novo.setStatus(atrasado ? "Atrasado" : "Em dia");
        } else {
            novo.setStatus("Em dia");
        }

        repository.save(novo);
        return ResponseEntity.ok().build();
    }

}
