package com.ConselhoDaComunidade.JudicialControl.controller;

import com.ConselhoDaComunidade.JudicialControl.DTO.RelatoriosPayload;
import com.ConselhoDaComunidade.JudicialControl.service.ReeducandoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RelatoriosController {

    @Autowired
    private ReeducandoService reeducandoService;

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/relatorios")
    public String relatorios(Model model) throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean canViewProximos5Dias = auth.getAuthorities().stream().anyMatch(a ->
                a.getAuthority().equals("ROLE_ADMIN") ||
                        a.getAuthority().equals("ROLE_USER") ||
                        a.getAuthority().equals("ROLE_VIEWER")
        );

        var resumoStatus = reeducandoService.getResumoStatus();
        var comparecimentosPorMes = reeducandoService.getComparecimentosPorMes();
        var tendencia = reeducandoService.getTendenciaAtrasosUltimosMeses();

        model.addAttribute("resumoStatus", resumoStatus);
        model.addAttribute("porMes", comparecimentosPorMes);
        model.addAttribute("tendenciaAtrasos", tendencia);

        model.addAttribute("canViewProximos5Dias", canViewProximos5Dias);

        if (canViewProximos5Dias) {
            var proximos5dias = reeducandoService.getProximosCumprimentos(5);
            model.addAttribute("proximos5dias", proximos5dias);
            model.addAttribute("qtdProximos5dias", proximos5dias.size());
        } else {
            model.addAttribute("proximos5dias", java.util.List.of());
            model.addAttribute("qtdProximos5dias", 0);
        }

        var payload = new RelatoriosPayload(resumoStatus, comparecimentosPorMes, tendencia);
        model.addAttribute("relatoriosJson", objectMapper.writeValueAsString(payload));

        return "relatorios";
    }



}
