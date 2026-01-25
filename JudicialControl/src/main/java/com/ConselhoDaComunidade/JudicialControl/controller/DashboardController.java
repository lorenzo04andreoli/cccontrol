package com.ConselhoDaComunidade.JudicialControl.controller;

import com.ConselhoDaComunidade.JudicialControl.entity.Arquivado;
import com.ConselhoDaComunidade.JudicialControl.entity.Reeducando;
import com.ConselhoDaComunidade.JudicialControl.repository.ArquivadoRepository;
import com.ConselhoDaComunidade.JudicialControl.service.ArquivadoService;
import com.ConselhoDaComunidade.JudicialControl.service.ReeducandoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private ReeducandoService reeducandoService;

    @Autowired
    private ArquivadoRepository arquivadoRepository;

    @Autowired
    private ArquivadoService arquivadoService;



    @GetMapping("/")
    public String dashboard(@RequestParam(required = false, defaultValue = "reeducandos") String tabela,
                            @RequestParam(required = false) String frequencia,
                            @RequestParam(required = false) String termo,
                            @RequestParam(required = false) String filtro,
                            @RequestParam(required = false) String competencia,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean canViewTabela = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equals("ROLE_USER") ||
                        a.getAuthority().equals("ROLE_VIEWER")
        );

        model.addAttribute("canViewTabela", canViewTabela);
        model.addAttribute("competencias", reeducandoService.listarCompetenciasExistentes());
        model.addAttribute("competencia", competencia);
        model.addAttribute("tabela", tabela);

        if (!canViewTabela) {

            model.addAttribute("reeducandos", java.util.List.of());
            model.addAttribute("totalReeducandos", 0);
            model.addAttribute("pagina", Page.empty());
            model.addAttribute("paginaAtual", 0);
            model.addAttribute("frequenciaSelecionada", frequencia);
            model.addAttribute("termoBusca", termo);
            model.addAttribute("filtroStatus", filtro);
            return "index";
        }

        if ("arquivados".equalsIgnoreCase(tabela)) {

            int size = 10;

            Page<Arquivado> paginaArq = arquivadoService.buscarArquivados(termo, page, size);

            model.addAttribute("pagina", paginaArq);
            model.addAttribute("arquivados", paginaArq.getContent());

            model.addAttribute("termoBusca", termo);
            model.addAttribute("paginaAtual", page);
            model.addAttribute("totalArquivados", paginaArq.getTotalElements());

            // (opcional) pra evitar confusão no front
            model.addAttribute("frequenciaSelecionada", null);
            model.addAttribute("filtroStatus", null);
            model.addAttribute("competencia", null);

            return "index";
        }



        int size = 10;
        Page<Reeducando> pagina = reeducandoService.buscarPorFiltros(frequencia, termo, filtro, competencia, page, size);

        model.addAttribute("pagina", pagina);
        model.addAttribute("reeducandos", pagina.getContent());
        model.addAttribute("frequenciaSelecionada", frequencia);
        model.addAttribute("termoBusca", termo);
        model.addAttribute("filtroStatus", filtro);
        model.addAttribute("paginaAtual", page);
        model.addAttribute("totalReeducandos", pagina.getTotalElements());

        return "index";


    }
}
