package com.ConselhoDaComunidade.JudicialControl.service;

import com.ConselhoDaComunidade.JudicialControl.DTO.CompetenciaOptionDto;
import com.ConselhoDaComunidade.JudicialControl.DTO.ProximoCumprimentoDto;
import com.ConselhoDaComunidade.JudicialControl.entity.Arquivado;
import com.ConselhoDaComunidade.JudicialControl.entity.Comparecimento;
import com.ConselhoDaComunidade.JudicialControl.entity.Reeducando;
import com.ConselhoDaComunidade.JudicialControl.repository.ArquivadoRepository;
import com.ConselhoDaComunidade.JudicialControl.repository.ComparecimentoRepository;
import com.ConselhoDaComunidade.JudicialControl.repository.ReeducandoRepository;
import com.ConselhoDaComunidade.JudicialControl.specification.ReeducandoSpecifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReeducandoService {

    private static final int JANELA_PENDENTE_DIAS = 5;

    @Autowired
    private ReeducandoRepository r;

    @Autowired
    private ComparecimentoRepository comparecimentoRepository;

    @Autowired private ArquivadoRepository arquivadoRepository;

    public List<Reeducando> listarTodosOrdenados(){
        return r.findAll(Sort.by(Sort.Direction.ASC, "nome"));
    }

    public void atualizarStatus(Long id, String novoStatus){
        Reeducando reeducando = r.findById(id).orElseThrow(() -> new RuntimeException("Não encontrado"));
        reeducando.setStatus(novoStatus);
        r.save(reeducando);
    }

    public List<Reeducando> listarAtrasados() {
        return r.findByStatusIgnoreCaseOrderByNomeAsc("Atrasado");
    }

    public List<Reeducando> listarPendentes() {
        return r.findByStatusIgnoreCaseOrderByNomeAsc("Pendente");
    }


    public List<Reeducando> listarPorStatus(String status) {
        return r.findByStatusIgnoreCase(status);
    }

    private int limiteDiasPorFrequencia(String frequencia){
        if (frequencia == null) return -1;

        return switch (frequencia.trim().toLowerCase()){
            case "mensal" -> 30;
            case "bimestral" -> 60;
            case "trimestral" -> 90;
            default -> -1;
        };
    }

    public String calcularStatus(Reeducando reeducando){
        if (reeducando.getDia() == null || reeducando.getFrequencia() == null){
            return "Em dia";
        }

        int limite = limiteDiasPorFrequencia(reeducando.getFrequencia());
        if (limite < 0 ) return "Em dia";

        long dias = ChronoUnit.DAYS.between(reeducando.getDia(), LocalDate.now());

        if (dias <= limite) return "Em dia";
        if (dias <= limite + JANELA_PENDENTE_DIAS) return "Pendente";
        return "Atrasado";
    }

    public boolean estaAtrasado(Reeducando reeducando){
        return "Atrasado".equalsIgnoreCase(calcularStatus(reeducando));
    }

    public boolean estaPendente(Reeducando reeducando) {
        return "Pendente".equalsIgnoreCase(calcularStatus(reeducando));
    }

    public List<Reeducando> filtrarPorFrequencia(String frequencia) {
        return r.findByFrequenciaIgnoreCase(frequencia);
    }

    public Page<Reeducando> buscarPorFiltros(String frequencia, String termo, String status,
                                             String competencia, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("nome").ascending());

        LocalDate inicioMes = null;
        LocalDate fimMes = null;

        boolean hasComp = competencia != null && !competencia.isBlank();
        if (hasComp) {

            YearMonth ym = YearMonth.parse(competencia);
            inicioMes = ym.atDay(1);
            fimMes = ym.plusMonths(1).atDay(1);
        }

        boolean hasTermo = termo != null && !termo.isBlank();
        String termoCpf = hasTermo ? termo.replaceAll("\\D", "") : "";
        boolean termoPareceCpf = hasTermo && !termoCpf.isBlank();

        String termoFinal = termoPareceCpf ? termoCpf : termo;

        Specification<Reeducando> spec = ReeducandoSpecifications.filtrar(
                frequencia,
                termoFinal,
                status,
                inicioMes,
                fimMes,
                termoPareceCpf
        );

        return r.findAll(spec, pageable);
    }


    public List<CompetenciaOptionDto> listarCompetenciasExistentes() {
        List<Object[]> rows = r.listarCompetenciasExistentes();

        List<CompetenciaOptionDto> out = new ArrayList<>();
        for (Object[] row : rows) {
            int ano = ((Number) row[0]).intValue();
            int mes = ((Number) row[1]).intValue();

            String value = String.format("%04d-%02d", ano, mes);
            String label = String.format("%02d/%04d", mes, ano);

            out.add(new CompetenciaOptionDto(value, label));
        }
        return out;
    }



    public Reeducando buscarPorId(Long id) {
        return r.findById(id).orElseThrow(() -> new RuntimeException("Reeducando não encontrado"));
    }

    public void atualizarDados(Long id, Map<String, String> dados) {
        Reeducando rcd = buscarPorId(id);
        rcd.setTelefone(dados.get("telefone"));

        if (dados.get("dia") != null && !dados.get("dia").isEmpty()) {
            LocalDate novoDia = LocalDate.parse(dados.get("dia"));


            rcd.setDia(novoDia);

            Comparecimento c = new Comparecimento();
            c.setReeducando(rcd);
            c.setData(novoDia);
            try {
                comparecimentoRepository.save(c);
            } catch (Exception ignored) { }

            rcd.setStatus(calcularStatus(rcd));
        }

        r.save(rcd);
    }


    public void excluir(Long id) {
        r.deleteById(id);
    }

    @Scheduled(cron = "0 */15 * * * *")
    public void verificarAtrasosAutomaticamente() {
        List<Reeducando> todos = r.findAll();
        List<Reeducando> alterados = new ArrayList<>();

        for (Reeducando re : todos) {
            String calc = calcularStatus(re);
            String atual = re.getStatus() == null ? "" : re.getStatus().trim();
            if (!atual.equalsIgnoreCase(calc)) {
                re.setStatus(calc);
                alterados.add(re);
            }
        }

        if (!alterados.isEmpty()) r.saveAll(alterados);

        System.out.println("[TAREFA AGENDADA] Verificação de atrasos concluída. Alterados=" + alterados.size());
    }

    public Map<String, Long> getResumoStatus() {
        long atrasados = r.countByStatusIgnoreCase("Atrasado");
        long pendentes = r.countByStatusIgnoreCase("Pendente");
        long emDia     = r.countByStatusIgnoreCase("Em dia");

        return Map.of(
                "atrasados", atrasados,
                "pendentes", pendentes,
                "emDia", emDia
        );
    }


    public Map<String, Long> getComparecimentosPorMes() {
        var rows = comparecimentoRepository.contarComparecimentosPorMes();


        List<Object[]> asc = new ArrayList<>(rows);
        Collections.reverse(asc);

        Map<String, Long> out = new LinkedHashMap<>();
        Locale ptBR = new Locale("pt", "BR");

        for (Object[] row : asc) {
            int ano = ((Number) row[0]).intValue();
            int mes = ((Number) row[1]).intValue();
            long total = ((Number) row[2]).longValue();

            String label = String.format("%02d/%04d", mes, ano);
            out.put(label, total);
        }

        return out;
    }


    @Transactional
    public void arquivar(Long reeducandoId, String observacao) {
        if (observacao == null || observacao.isBlank()) {
            throw new RuntimeException("Observação é obrigatória.");
        }

        Reeducando re = r.findById(reeducandoId)
                .orElseThrow(() -> new RuntimeException("Reeducando não encontrado"));

        String cpf = re.getCpf() == null ? null : re.getCpf().replaceAll("\\D", "");


        if (arquivadoRepository.existsByCpf(cpf)) {
            throw new RuntimeException("Já existe um arquivado com este CPF.");
        }

        if (cpf == null || cpf.isBlank()) {
            throw new RuntimeException("CPF inválido.");
        }

        Arquivado arq = new Arquivado();
        arq.setNome(re.getNome());
        arq.setCpf(cpf);
        arq.setTelefone(re.getTelefone());
        arq.setDia(re.getDia());
        arq.setFrequencia(re.getFrequencia());
        arq.setAutos(re.getAutos());
        arq.setObservacao(observacao.trim());

        arquivadoRepository.save(arq);

        r.delete(re);
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


    public List<ProximoCumprimentoDto> getProximosCumprimentos(int diasJanela) {
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(diasJanela);

        return r.findAll().stream()
                .filter(re -> re.getDia() != null && re.getFrequencia() != null)
                .map(re -> {
                    LocalDate prox = calcularProximoComparecimento(re.getDia(), re.getFrequencia());
                    long diasRestantes = ChronoUnit.DAYS.between(hoje, prox);
                    return new ProximoCumprimentoDto(
                            re.getId(),
                            re.getNome(),
                            re.getCpf(),
                            re.getTelefone(),
                            re.getFrequencia(),
                            re.getDia(),
                            prox,
                            diasRestantes
                    );
                })
                .filter(dto -> !dto.proximoComparecimento().isBefore(hoje) &&
                        !dto.proximoComparecimento().isAfter(limite))
                .sorted(Comparator.comparing(ProximoCumprimentoDto::proximoComparecimento)
                        .thenComparing(ProximoCumprimentoDto::nome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private LocalDate calcularProximoComparecimento(LocalDate ultimo, String frequencia) {
        int limiteDias = limiteDiasPorFrequencia(frequencia);
        if (limiteDias < 0) return ultimo; // fallback
        return ultimo.plusDays(limiteDias);
    }


}