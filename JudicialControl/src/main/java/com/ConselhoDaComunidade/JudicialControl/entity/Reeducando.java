package com.ConselhoDaComunidade.JudicialControl.entity;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reeducandos")
public class Reeducando {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true, nullable = false, length = 11)
    private String cpf;
    private String telefone;

    @Column(name = "dia")
    private LocalDate dia;

    private String frequencia;
    private String status;

    private Integer diaFixo;

    @Column(name = "autos", length = 50)
    private String autos;

    @OneToMany(mappedBy = "reeducando", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comparecimento> comparecimentos = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public LocalDate getDia() {
        return dia;
    }

    public void setDia(LocalDate dia) {
        this.dia = dia;
    }

    public String getFrequencia() {
        return frequencia;
    }

    public void setFrequencia(String frequencia) {
        this.frequencia = frequencia;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getDiaFixo() {
        return diaFixo;
    }

    public void setDiaFixo(Integer diaFixo) {
        this.diaFixo = diaFixo;
    }

    public String getAutos() {
        return autos;
    }

    public void setAutos(String autos) {
        this.autos = autos;
    }
}
