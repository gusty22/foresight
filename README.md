# Foresight API & Web 🚀

Sistema de Business Intelligence e Gestão Comercial.

## 🛠️ Pré-requisitos
Para rodar este projeto, você precisará ter instalado na sua máquina:
* **Java 17**
* **Node.js 20+** (Para o frontend Angular)
* **Docker Desktop** (Para subir o banco de dados PostgreSQL automaticamente)

## ⚙️ Como rodar o projeto localmente

### 1. Banco de Dados
Não é necessário instalar o PostgreSQL. Apenas mantenha o seu Docker aberto. O Spring Boot fará o start automático dos containers definidos no `docker-compose.yml`.

### 2. Rodando o Backend (Spring Boot)
Abra o terminal na pasta do backend e utilize o Maven Wrapper:

No Windows:
> ./mvnw.cmd spring-boot:run

No Mac/Linux:
> ./mvnw spring-boot:run

A API estará disponível em: `http://localhost:8080`

### 3. Rodando o Frontend (Angular)
Abra um novo terminal na pasta do frontend:

> npm install
> npm start

Acesse a aplicação em: `http://localhost:4200`