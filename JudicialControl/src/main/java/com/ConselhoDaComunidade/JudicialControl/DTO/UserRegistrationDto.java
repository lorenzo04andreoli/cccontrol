package com.ConselhoDaComunidade.JudicialControl.DTO;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class UserRegistrationDto {
    @NotEmpty(message = "Nome é obrigatório")
    private String nome;

    @NotEmpty(message = "CPF é obrigatório")
    private String cpf;

    @NotEmpty(message = "Senha é obrigatória")
    @Size(min = 12, message = "A senha deve ter pelo menos 12 caracteres")
    private String senha;

    @NotEmpty(message = "Confirmação de senha é obrigatória")
    private String confirmPassword;

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

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public boolean isCpfValido(){
        if (cpf == null || !cpf.matches("\\d{11}")) return false;

        if (cpf.chars().distinct().count() == 1) return false;

        try{
            int soma1 = 0, soma2 = 0;
            for (int i = 0; i < 9; i++){
                int num = cpf.charAt(i) -'0';
                soma1 += num * (10 - i);
                soma2 += num * (11 - i);
            }

            int dig1 = 11 - (soma1 % 11);
            dig1 = (dig1 > 9) ? 0 : dig1;
            soma2 += dig1 * 2;
            int dig2 = 11 - (soma2 % 11);
            dig2 = (dig2 > 9) ? 0 : dig2;

            return dig1 == (cpf.charAt(9) - '0') && dig2 == (cpf.charAt(10) - '0');
        }catch (Exception e){
            return false;
        }
    }
}
