package com.ConselhoDaComunidade.JudicialControl.repository;

import com.ConselhoDaComunidade.JudicialControl.entity.Arquivado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ArquivadoRepository extends JpaRepository<Arquivado, Long>, JpaSpecificationExecutor<Arquivado> {
    boolean existsByCpf(String cpf);

    Page<Arquivado> findByNomeContainingIgnoreCaseOrCpfContainingIgnoreCase(String nome, String cpf, Pageable pageable);
}
