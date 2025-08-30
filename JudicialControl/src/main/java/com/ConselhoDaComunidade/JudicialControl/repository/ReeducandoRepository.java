package com.ConselhoDaComunidade.JudicialControl.repository;

import com.ConselhoDaComunidade.JudicialControl.entity.Reeducando;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReeducandoRepository extends JpaRepository<Reeducando, Long> {
    List<Reeducando> findByStatus(String status);

    List<Reeducando> findByFrequenciaIgnoreCase(String frequencia);

    List<Reeducando> findByNomeContainingIgnoreCaseOrCpfContainingIgnoreCase(String nome, String cpf);

    Page<Reeducando> findAll(Pageable pageable);

}
