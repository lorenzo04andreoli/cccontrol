package com.ConselhoDaComunidade.JudicialControl.DTO;

public class UserListItem {
    private String nome;
    private String cpf;
    private boolean online;

    public UserListItem(String nome, String cpf, boolean online) {
        this.nome = nome;
        this.cpf = cpf;
        this.online = online;
    }

    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public boolean isOnline() { return online; }
}
