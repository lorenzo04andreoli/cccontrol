package com.ConselhoDaComunidade.JudicialControl.repository;

import com.ConselhoDaComunidade.JudicialControl.entity.Reeducando;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReeducandoRepository extends JpaRepository<Reeducando, Long>, JpaSpecificationExecutor<Reeducando> {

    boolean existsByCpf(String cpf);

    long countByStatusIgnoreCase(String status);

    List<Reeducando> findByStatusIgnoreCase(String status);

    List<Reeducando> findByFrequenciaIgnoreCase(String frequencia);

    List<Reeducando> findByStatusIgnoreCaseOrderByNomeAsc(String status);

    @Query(value = """
    SELECT DISTINCT YEAR(dia) AS ano, MONTH(dia) AS mes
    FROM reeducandos
    WHERE dia IS NOT NULL
    ORDER BY ano DESC, mes DESC
    """, nativeQuery = true)
    List<Object[]> listarCompetenciasExistentes();

}
