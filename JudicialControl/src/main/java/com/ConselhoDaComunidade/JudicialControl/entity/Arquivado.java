package com.ConselhoDaComunidade.JudicialControl.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "arquivados")
public class Arquivado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(unique = true, nullable = false, length = 11)
    private String cpf;

    private String telefone;

    private LocalDate dia;
    private String frequencia;

    @Column(length = 50)
    private String autos;

    @Column(nullable = false, length = 500)
    private String observacao;

    @Column(name = "data_arquivamento", nullable = false)
    private LocalDateTime dataArquivamento = LocalDateTime.now();


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

    public String getAutos() {
        return autos;
    }

    public void setAutos(String autos) {
        this.autos = autos;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public LocalDateTime getDataArquivamento() {
        return dataArquivamento;
    }

    public void setDataArquivamento(LocalDateTime dataArquivamento) {
        this.dataArquivamento = dataArquivamento;
    }
}
