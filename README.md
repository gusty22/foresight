# Foresight - Business Intelligence & Gestão Comercial 🚀

**Estado do projeto:** implementação em desenvolvimento. O endereço de deploy anteriormente mencionado era apenas uma intenção, não uma demonstração pública verificada.

O **Foresight** é uma plataforma inovadora de gestão comercial e financeira projetada para micro e pequenas empresas, com um diferencial de módulo nativo para **Gestão de Sociedade e Investimentos (Equity)**.

---

## 🏗️ 1. Arquitetura do Sistema

O sistema adota uma arquitetura **Cliente-Servidor (Desacoplada)** gerenciada em um **Monorepo**.

* Não é um monólito tradicional, pois o Frontend e o Backend são aplicações completamente separadas e independentes.
* O repositório contém duas pastas principais:
* `/foresight-api` (Backend)
* `/foresight-web` (Frontend)



---

## 🗄️ 2. Banco de Dados

* **SGBD:** PostgreSQL 15
* **Nome do Banco de Dados:** `foresight_db` (criado e orquestrado automaticamente via Docker Compose).
* O versionamento do banco de dados e a criação automática das tabelas são feitos através do **Flyway**.

---

## 🛠️ 3. Stack Tecnológica e Portas

O projeto possui o Frontend separado do Backend. As tecnologias e portas utilizadas são:

### Backend (API REST)

* **Linguagem:** Java 17
* **Framework:** Spring Boot 3.4.2
* **Segurança:** Spring Security com JWT
* **Porta de Execução Local (IDE):** `8080`
* **Porta Exposta no Docker:** `8081`
* **Documentação:** Swagger UI (acessível via Docker em `http://localhost:8081/swagger-ui.html` ou `/swagger-ui/index.html`)

### Frontend (SPA - Single Page Application)

* **Tecnologia:** Angular (com TypeScript e Node.js 20+)
* **Estilização:** Bootstrap 5
* **Servidor Web (Docker):** Nginx Alpine
* **Porta Exposta no Docker:** `4200` (Mapeada para a porta interna 80 do Nginx)

---

## 🐳 4. Como Rodar o Projeto Localmente (Para Avaliação)

A infraestrutura foi construída com foco em **Frictionless Onboarding** (integração sem atrito).

> **Não é necessário instalar Java, Maven, Node.js ou PostgreSQL na máquina hospedeira.**

Para rodar o sistema completo (**Banco de Dados + API + Web**) de uma única vez, basta ter o **Docker Desktop** em execução e seguir estes passos:

### Passo 1

Abra o terminal na raiz do projeto (onde está o arquivo `docker-compose.yml`).

### Passo 2

Execute o comando abaixo para construir e subir os contêineres:

```bash
docker compose up -d --build

```

### Passo 3

Aguarde a finalização do processo. O Docker irá baixar as imagens necessárias, compilar o backend via Maven e compilar o frontend via Node.js.

### Passo 4

Acesse a aplicação no navegador:

```text
http://localhost:4200

```

### Passo 5

A documentação da API (Swagger) estará disponível em:

```text
http://localhost:8081/swagger-ui.html

```

### Nota para Desenvolvedores

Caso deseje rodar a API via IDE (ex.: IntelliJ IDEA) para desenvolvimento, o Spring Boot está configurado com a biblioteca `spring-boot-docker-compose`, que levanta o banco de dados PostgreSQL automaticamente sem necessidade de comandos manuais, bastando executar a aplicação pela IDE.

---

## 🧪 5. Acesso e Dados para Teste

O banco de dados é populado automaticamente ao subir os contêineres, graças às migrations do Flyway (arquivos `.sql` localizados em `src/main/resources/db/migration`) e as classes de Seeder da API.

### Acesso e segurança na execução local

A API possui provisionamento de administrador na classe `AdminSeederConfig`. O código atual define valores padrão de desenvolvimento quando não são fornecidas as propriedades `app.security.superadmin.email` e `app.security.superadmin.password`.

**Importante:** defina credenciais próprias e fortes antes de executar em um ambiente exposto. Nunca utilize credenciais de desenvolvimento em produção. O banco PostgreSQL do `docker-compose.yml` também utiliza usuário e senha de exemplo e expõe a porta 5432; adapte essas configurações para um ambiente seguro.

A aplicação possui código de autenticação e autorização, mas este README não substitui uma auditoria de segurança. Não há validação de implantação pública ou de todos os fluxos neste documento.

---

## ✅ Resumo Rápido

| Serviço | Tecnologia | Porta Host |
| --- | --- | --- |
| Frontend | Angular + Nginx | `4200` |
| Backend | Spring Boot 3.4.2 | `8081` |
| Banco de Dados | PostgreSQL 15 | `5432` |
| Documentação API | Swagger UI | `8081/swagger-ui.html` |

### Comando Principal

```bash
docker compose up -d --build

```


## Observações de implementação

A árvore do repositório contém módulos de clientes, produtos, investimentos, vendas, autenticação, auditoria e componentes de backoffice. Trata-se de evidência de código e estrutura, **não de uma certificação de que todos os fluxos estejam completos ou testados**.

Frontend e backend também estão organizados em diretórios separados dentro deste repositório. Existe ainda um [repositório independente de frontend](https://github.com/gusty22/foresight-web); não presuma que as duas versões estejam sincronizadas sem comparar o código.
