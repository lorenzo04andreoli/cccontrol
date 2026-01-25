package com.ConselhoDaComunidade.JudicialControl.repository;

import com.ConselhoDaComunidade.JudicialControl.entity.Comparecimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ComparecimentoRepository extends JpaRepository<Comparecimento, Long> {

    @Query(value = """
        SELECT YEAR(c.data) AS ano, MONTH(c.data) AS mes, COUNT(*) AS total
        FROM comparecimentos c
        WHERE c.data IS NOT NULL
        GROUP BY ano, mes
        ORDER BY ano DESC, mes DESC
    """, nativeQuery = true)
    List<Object[]> contarComparecimentosPorMes();
}
