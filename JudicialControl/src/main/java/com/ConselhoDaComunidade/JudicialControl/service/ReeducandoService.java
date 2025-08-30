package com.ConselhoDaComunidade.JudicialControl.service;

import com.ConselhoDaComunidade.JudicialControl.entity.Reeducando;
import com.ConselhoDaComunidade.JudicialControl.repository.ReeducandoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReeducandoService {
    @Autowired
    private ReeducandoRepository r;

    public List<Reeducando> listarTodosOrdenados(){
        return r.findAll(Sort.by(Sort.Direction.ASC, "nome"));
    }

    public void atualizarStatus(Long id, String novoStatus){
        Reeducando reeducando = r.findById(id).orElseThrow(() -> new RuntimeException("Não encontrado"));
        reeducando.setStatus(novoStatus);
        r.save(reeducando);
    }

    public List<Reeducando> listarAtrasados(){
        return r.findAll().stream()
                .filter(this::estaAtrasado)
                .sorted(Comparator.comparing(Reeducando::getNome, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    public List<Reeducando> listarPorStatus(String status) {
        return r.findByStatus(status);
    }

    public boolean estaAtrasado(Reeducando reeducando){
        if (reeducando.getDia() == null || reeducando.getFrequencia() == null) return false;

        LocalDate dataUltimoComparecimento = reeducando.getDia();
        LocalDate hoje = LocalDate.now();
        long dias = ChronoUnit.DAYS.between(dataUltimoComparecimento, hoje);

        switch (reeducando.getFrequencia().toLowerCase()){
            case "mensal":
                return dias > 30;
            case "bimestral":
                return dias > 60;
            case "trimestral":
                return dias > 90;
            default:
                return false;
        }
    }

    public List<Reeducando> filtrarPorFrequencia(String frequencia) {
        return r.findByFrequenciaIgnoreCase(frequencia);
    }

    public List<Reeducando> buscarPorNomeOuCpf(String termo) {
        return r.findByNomeContainingIgnoreCaseOrCpfContainingIgnoreCase(termo, termo);
    }

    public List<Reeducando> buscarPorFiltros(String frequencia, String termo) {
        List<Reeducando> lista = r.findAll();

        if (frequencia != null && !frequencia.isEmpty()) {
            lista = lista.stream()
                    .filter(r -> r.getFrequencia().equalsIgnoreCase(frequencia))
                    .collect(Collectors.toList());
        }

        if (termo != null && !termo.isEmpty()) {
            String termoLower = termo.toLowerCase();
            lista = lista.stream()
                    .filter(r -> r.getNome().toLowerCase().contains(termoLower)
                            || r.getCpf().toLowerCase().contains(termoLower))
                    .collect(Collectors.toList());
        }

        return lista;
    }

    public Page<Reeducando> buscarPorFiltros(String frequencia, String termo, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("nome").ascending());
        List<Reeducando> lista = r.findAll();

        if (frequencia != null && !frequencia.isEmpty()) {
            lista = lista.stream()
                    .filter(r -> r.getFrequencia().equalsIgnoreCase(frequencia))
                    .collect(Collectors.toList());
        }

        if (termo != null && !termo.isEmpty()) {
            String termoLower = termo.toLowerCase();
            lista = lista.stream()
                    .filter(r -> r.getNome().toLowerCase().contains(termoLower)
                            || r.getCpf().toLowerCase().contains(termoLower))
                    .collect(Collectors.toList());
        }

        if (status != null && !status.isBlank()) {
            String statusFiltrado = status.trim().toLowerCase();
            lista = lista.stream()
                    .filter(r -> r.getStatus() != null &&
                            r.getStatus().trim().toLowerCase().equals(statusFiltrado))
                    .collect(Collectors.toList());
        }

        lista = lista.stream()
                .sorted(Comparator.comparing(Reeducando::getNome, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), lista.size());

        if (start > lista.size()) {
            return new PageImpl<>(Collections.emptyList(), pageable, lista.size());
        }

        return new PageImpl<>(lista.subList(start, end), pageable, lista.size());
    }

    public Reeducando buscarPorId(Long id) {
        return r.findById(id).orElseThrow(() -> new RuntimeException("Reeducando não encontrado"));
    }

    public void atualizarDados(Long id, Map<String, String> dados) {
        Reeducando rcd = buscarPorId(id);
        rcd.setTelefone(dados.get("telefone"));

        if (dados.get("dia") != null && !dados.get("dia").isEmpty()) {
            rcd.setDia(LocalDate.parse(dados.get("dia")));

            boolean atrasado = estaAtrasado(rcd);
            rcd.setStatus(atrasado ? "Atrasado" : "Em dia");
        }

        r.save(rcd);
    }

    public void excluir(Long id) {
        r.deleteById(id);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void verificarAtrasosAutomaticamente() {
        List<Reeducando> todos = r.findAll();

        for (Reeducando reeducando : todos) {
            boolean atrasado = estaAtrasado(reeducando);

            if (atrasado && !"Atrasado".equalsIgnoreCase(reeducando.getStatus())) {
                reeducando.setStatus("Atrasado");
                r.save(reeducando);
            } else if (!atrasado && !"Em dia".equalsIgnoreCase(reeducando.getStatus())) {
                reeducando.setStatus("Em dia");
                r.save(reeducando);
            }
        }

        System.out.println("[TAREFA AGENDADA] Verificação de atrasos concluída.");
    }

    public Map<String, Long> getResumoStatus() {
        List<Reeducando> todos = r.findAll();
        long atrasados = todos.stream().filter(this::estaAtrasado).count();
        long emDia = todos.size() - atrasados;

        return Map.of(
                "atrasados", atrasados,
                "emDia", emDia
        );
    }

    public Map<String, Long> getCadastradosPorMes() {
        LocalDate hoje = LocalDate.now();
        YearMonth seisMesesAtras = YearMonth.from(hoje).minusMonths(5);

        return r.findAll().stream()
                .filter(re -> {
                    YearMonth ym = YearMonth.from(re.getDia());
                    return !ym.isBefore(seisMesesAtras) && !ym.isAfter(YearMonth.from(hoje));
                })
                .collect(Collectors.groupingBy(
                        re -> YearMonth.from(re.getDia()),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        e -> e.getKey().getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR")),
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    public Map<String, Long> getTendenciaAtrasosUltimosMeses() {
        LocalDate hoje = LocalDate.now();
        YearMonth seisMesesAtras = YearMonth.from(hoje).minusMonths(5);

        return r.findAll().stream()
                .filter(this::estaAtrasado)
                .filter(re -> {
                    YearMonth ym = YearMonth.from(re.getDia());
                    return !ym.isBefore(seisMesesAtras) && !ym.isAfter(YearMonth.from(hoje));
                })
                .collect(Collectors.groupingBy(
                        re -> YearMonth.from(re.getDia()),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(
                        e -> e.getKey().getMonth().getDisplayName(TextStyle.FULL, new Locale("pt", "BR")),
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

}
