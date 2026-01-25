package com.ConselhoDaComunidade.JudicialControl.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "comparecimentos",
        uniqueConstraints = @UniqueConstraint(columnNames = {"reeducando_id", "data"}))
public class Comparecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "reeducando_id")
    private Reeducando reeducando;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    public Long getId() { return id; }

    public Reeducando getReeducando() { return reeducando; }
    public void setReeducando(Reeducando reeducando) { this.reeducando = reeducando; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }
}
