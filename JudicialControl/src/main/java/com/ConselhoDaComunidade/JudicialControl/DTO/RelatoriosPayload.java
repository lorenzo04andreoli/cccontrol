package com.ConselhoDaComunidade.JudicialControl.DTO;

import java.util.Map;

public class RelatoriosPayload {
    private Map<String, Long> resumoStatus;
    private Map<String, Long> porMes;
    private Map<String, Long> tendencia;

    public RelatoriosPayload(Map<String, Long> resumoStatus,
                             Map<String, Long> porMes,
                             Map<String, Long> tendencia) {
        this.resumoStatus = resumoStatus;
        this.porMes = porMes;
        this.tendencia = tendencia;
    }

    public Map<String, Long> getResumoStatus() {
        return resumoStatus;
    }

    public Map<String, Long> getPorMes() {
        return porMes;
    }

    public Map<String, Long> getTendencia() {
        return tendencia;
    }
}
