# CCControl – Sistema de Controle de Reeducandos

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)
![Docker](https://img.shields.io/badge/Docker-Enabled-blue)
![AWS](https://img.shields.io/badge/AWS-EC2-orange)


O **CCControl** é um sistema web desenvolvido para automatizar o controle de reeducandos que cumprem penas alternativas e devem comparecer periodicamente em juízo, sendo acompanhados pelo **Conselho da Comunidade**.

Antes do sistema, todo o processo era **manual**, exigindo a análise individual de mais de 200 fichas físicas todo fim de mês.  
Hoje, o controle é **automatizado, seguro e centralizado**, reduzindo erros humanos e aumentando a eficiência operacional.

> ⚠️ Este sistema foi desenvolvido para um **ambiente real de trabalho** e está em uso operacional,
> respeitando boas práticas de segurança e controle de acesso.

---

## 🎯 Objetivo do Sistema

- Automatizar o controle de comparecimentos judiciais  
- Identificar automaticamente:
  - Reeducandos **em dia**
  - **Pendentes**
  - **Atrasados**
- Reduzir trabalho manual e retrabalho
- Oferecer **relatórios e gráficos gerenciais**
- Apoiar decisões rápidas e preventivas

---

## 🧠 Regra de Negócio 

Cada reeducando possui:
- Data do último comparecimento
- Frequência definida judicialmente

### 📆 Frequências suportadas

| Frequência  | Intervalo |
|------------|-----------|
| Mensal     | 30 dias   |
| Bimestral  | 60 dias   |
| Trimestral | 90 dias   |

### Cálculo de Status

- Dentro do prazo → **Em dia**
- Prazo + até 5 dias → **Pendente**
- Prazo + 6 dias ou mais → **Atrasado**

> O sistema executa uma **verificação automática agendada** para recalcular os status periodicamente, sem intervenção manual.

---

## ⚙️ Funcionalidades Principais

### 👥 Gestão de Reeducandos
- Cadastro e edição
- Arquivamento com histórico
- Registro automático de comparecimentos
- Paginação e ordenação

### 🔎 Filtros Avançados
- Nome ou CPF
- Frequência
- Status
- Competência (mês/ano)

### 📊 Relatórios
- Resumo geral (Atrasados, Pendentes e Em dia)
- Comparecimentos por mês
- Tendência de atrasos
- Próximos comparecimentos (janela configurável)

## 🔐 Segurança

- Autenticação por **CPF e senha**
- Senhas armazenadas com **hash BCrypt**
- **Autenticação em dois fatores (2FA)**
- Controle de **sessão única ativa por usuário**
- Proteções adicionais:
  - CSRF
  - Content Security Policy (CSP)
  - Headers de segurança
  - HTTPS com certificado SSL

### 👤 Perfis de Acesso

O sistema utiliza **controle de acesso baseado em papéis (RBAC)**, garantindo que cada usuário visualize e execute apenas as ações permitidas pelo seu perfil.

- **`ADMIN`**  
  Administrador do sistema.  
  Possui acesso total à aplicação, incluindo:
  - Cadastro, edição e exclusão de reeducandos
  - Arquivamento de registros
  - Visualização completa de relatórios
  - Cadastro e gerenciamento de usuários e permissões

- **`USER`**  
  Colaborador operacional.  
  Pode:
  - Cadastrar e editar reeducandos
  - Atualizar comparecimentos
  - Visualizar relatórios  
  - Não possui acesso à administração de usuários

- **`VIEWER`**  
  Perfil consultivo.  
  Pode:
  - Visualizar tabelas de reeducandos
  - Acessar relatórios e gráficos  
  - Não pode realizar operações de cadastro, edição ou exclusão (CRUD)

- **`AUDIENCE`**  
  Perfil de visualização restrita.  
  Pode:
  - Acessar apenas **relatórios consolidados**
  - Não possui acesso a dados sensíveis individuais  
  - Indicado para auditorias, apresentações ou acompanhamento institucional

> Essa separação garante **segurança, rastreabilidade e conformidade**, evitando acessos indevidos e exposição desnecessária de dados.

---

## 🧰 Tecnologias Utilizadas

### Backend
- **Java 17**
- **Spring Boot**
- Spring Security
- Spring Data JPA
- Hibernate ORM
- Scheduler (`@Scheduled`)
- Specification API (filtros dinâmicos)

### Frontend
- Thymeleaf
- HTML5 / CSS3
- JavaScript
- Chart.js

### Banco de Dados
- **MySQL 8**
- Persistência com Docker Volume

### Infraestrutura & DevOps
- **Docker**
- Docker Compose
- **AWS EC2**
- HTTPS com **SSL (keystore.p12)**
- Containers isolados
- Rede Docker interna

---

## 🧱 Arquitetura da Aplicação

A aplicação roda em uma **instância AWS EC2**, utilizando **Docker Compose** para orquestrar os containers de aplicação e banco de dados.

### 📐 Diagrama de Arquitetura

<p align="center">
  <img src="/JudicialControl/docs/architecture.png" alt="Arquitetura do Sistema" width="900"/>
</p>

> A arquitetura é baseada em Docker e executada em uma instância AWS EC2,
> utilizando Spring Boot e MySQL em containers isolados.
> com serviços gerenciados da AWS.

---

### 🔁 Fluxo de Comunicação

1. Usuário acessa o sistema via HTTPS (porta 8443)
2. EC2 recebe a requisição
3. Container Spring Boot processa a lógica
4. Comunicação interna com MySQL via Docker Network
5. Dados persistidos em Docker Volume
6. Certificado SSL gerenciado via keystore

---

## 🐳 Docker & Containers

### Containers
- `judicialcontrol-app` → Spring Boot (Java)
- `judicialcontrol-db` → MySQL 8

### Comunicação interna

A comunicação entre os containers ocorre através da **Docker Network**, utilizando o hostname do serviço definido no `docker-compose`.

Exemplo:

```text
jdbc:mysql://db:3306/judicialcontrol
```
Isso elimina a necessidade de exposição do banco de dados para a rede externa.


### Benefícios
- Isolamento de responsabilidades
- Reprodutibilidade
- Deploy simplificado
- Facilidade de manutenção

---

## ☁️ Deploy na AWS

- EC2 Linux (Ubuntu)
- Tipo de instância: `t3.small`
- Docker instalado no host
- Docker Compose para orquestração
- Porta 8443 liberada no Security Group
- HTTPS configurado diretamente no Spring Boot

---

## 🚀 Aprendizados

Este projeto consolidou conhecimentos em:
- Arquitetura backend
- Segurança web avançada
- Autenticação em dois fatores
- Docker em ambiente real
- Deploy em nuvem (AWS)
- Regras de negócio complexas
- Refatoração contínua

> Foram **7 meses de desenvolvimento**, errando, aprendendo, refatorando e evoluindo.

---

## 👨‍💻 Autor

**Lorenzo Andreoli**  
Desenvolvedor Backend | Java | Spring Boot | MySQL | Docker | AWS  

Projeto desenvolvido para resolver um problema real, com impacto direto na rotina do Conselho da Comunidade.

