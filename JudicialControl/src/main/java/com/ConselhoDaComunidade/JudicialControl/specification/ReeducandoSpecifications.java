package com.ConselhoDaComunidade.JudicialControl.specification;

import com.ConselhoDaComunidade.JudicialControl.entity.Reeducando;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReeducandoSpecifications {

    public static Specification<Reeducando> filtrar(
            String frequencia,
            String termo,
            String status,
            LocalDate inicioMes,
            LocalDate fimMes,
            boolean termoPareceCpf
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Frequência
            if (frequencia != null && !frequencia.isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("frequencia")),
                        frequencia.trim().toLowerCase()
                ));
            }

            // Status
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(
                        cb.lower(root.get("status")),
                        status.trim().toLowerCase()
                ));
            }

            // Competência (dia entre [inicioMes, fimMes))
            if (inicioMes != null && fimMes != null) {
                // dia >= inicioMes AND dia < fimMes
                predicates.add(cb.greaterThanOrEqualTo(root.get("dia"), inicioMes));
                predicates.add(cb.lessThan(root.get("dia"), fimMes));
            }

            // Termo (nome ou cpf)
            if (termo != null && !termo.isBlank()) {
                String t = termo.trim().toLowerCase();
                String cpfDigits = termo.replaceAll("\\D", "");

                Predicate pNome = cb.like(cb.lower(root.get("nome")), "%" + t + "%");
                Predicate pCpf  = cb.like(root.get("cpf"), "%" + (cpfDigits.isBlank() ? t : cpfDigits) + "%");

                predicates.add(cb.or(pNome, pCpf));
            }


            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
