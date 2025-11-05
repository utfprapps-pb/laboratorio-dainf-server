# Laboratório DAINF - Backend

Repositório do servidor backend da aplicação de gerenciamento de laboratórios do Departamento Acadêmico de Informática (DAINF) da UTFPR - Campus Pato Branco.

Este sistema fornece uma API RESTful construída com Spring Boot, responsável por gerenciar laboratórios, usuários e reservas.

---

## ⚙️ Tecnologias Utilizadas

- **Java 17**
- **Spring Boot**
    - Spring Web
    - Spring Data JPA
    - Spring Security
- **Banco de Dados**: PostgreSQL 
- **Maven** ou **Gradle**
- **Lombok**
- **Swagger/OpenAPI** (documentação da API, se disponível)
- **Flyway**: Migração do banco de dados.

---

## 📦 Como Executar o Projeto

### 1. Pré-requisitos

- Java 17+
- Maven ou Gradle
- IDE (IntelliJ, Eclipse, VS Code)
- Criar um banco de dados no PostgresSQL

### 2. Clone o repositório

```bash
git clone https://github.com/utfprapps-pb/laboratorio-dainf-server.git
cd laboratorio-dainf-server
```

### 3. Configure o ambiente

Crie um arquivo chamado **.env** em src/main/resources/, com as configurações básicas, conforme o arquivo **.env.example**. 
Nesse arquivo devem ser informadas os dados da conexão com o banco de dados conforme os dados do SGBD utilizado.

Nesse arquivo também deve ser configurado os dados do Minio para teste de upload de arquivos, 
o arquivo **docker-compose-minio.yml** possui um exemplo de criação de uma instância minio em um contêiner Docker. 

Também é necessário criar uma conta de email para testes, os dados da conta de email também devem ser informados no arquivo 
**.env**, o GMail permite configuração de conta de email para envio por aplicações externas no endereço: [https://myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords). 

```properties
SERVER_PORT=8099
SPRING_PROFILES_ACTIVE=dev

DATABASE_URL=jdbc:postgresql://localhost:5432/dainf_labs
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres

GOOGLE_CLIENT_ID=123456
GOOGLE_CLIENT_SECRET=123456

UTFPR_TOKEN_SECRET=Utfpr@D4!NF

UTFPR_EMAIL_ADDRESS=email@utfpr.edu.br
UTFPR_EMAIL_PASSWORD=123456
UTFPR_EMAIL_USERNAME=email
UTFPR_EMAIL_HOST=smtp.utfpr.edu.br
#UTFPR_EMAIL_HOST=200.19.73.102
UTFPR_EMAIL_PORT=587

UTFPR_FRONT_URL=http://localhost:4200/#

MINIO_ENDPOINT=http://127.0.0.1:9000
MINIO_PORT=9000
MINIO_ACCESS_KEY=minio@admin
MINIO_SECRET_KEY=minio@admin
MINIO_SECURE=false
MINIO_BUCKET_NAME=dainf-labs
```


### 4. Execute a aplicação

Iniciar a aplicação pelo arquivo **ServerApplication** se utilizando uma IDE como uma aplicação Spring Boot. ou com o Maven executando:

```bash
./mvnw spring-boot:run
```

### 📁 Estrutura do Projeto

```bash
br.com.utfpr.gerenciamento.server
src/
├── main/
│   ├── br/com/utfpr/gerenciamento/server
│   │   ├── component/
│   │   ├── config/
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── enumeration/
│   │   ├── error/
│   │   ├── mienio/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── security/
│   │   ├── service/
│   │   ├── util/
│   │   └── ServerApplication.java
│   └── resources/
│       ├── db/
│       ├── templates/
│       ├── .env
│       └── application.properties (-dev, -prod, -test)
└── test/
```

### 📄 Licença
Projeto desenvolvido com fins acadêmicos no âmbito da UTFPR - Campus Pato Branco.
Licenciado sob os termos da licença MIT.

---

## <img src="https://sistemas2.utfpr.edu.br/assets/favicon.svg" alt="UTFPR Logo" width="20" /> Desenvolvido por alunos e professores da UTFPR-PB.

#### A aplicação faz parte do projeto **Bowser: desenvolvimento de softwares de código aberto para a comunidade**.

 - Professor: Vinicius Pegorini
 - Aluno: Gustavo Henrique Lopes Spachuk Zaffani (TCC - Análise e Desenvolvimento de Sistemas)