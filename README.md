# Portal Conecta - Mapa de Sala Backend

API e lógica de negócio do Mapa de Sala.

**Pré-requisitos:** Java 21 e Maven (via wrapper `mvnw` incluído no projeto).

## Arquitetura e Estrutura de Diretórios

O projeto segue uma arquitetura modular inspirada em conceitos de Clean Architecture, focada em separar a lógica de negócio das configurações globais, mas adaptada pragmaticamente para se alinhar ao padrão do ecossistema do Hub.

O código está dividido em dois pacotes principais dentro de `src/main/java/...`:
* **`module.seat_map`**: Contém todo o escopo de negócio do Mapa de Sala.
* **`shared`**: Contém configurações transversais, tratamento global de exceções, segurança e contextos compartilhados.

### A Decisão sobre a Camada de Infraestrutura
Para manter a consistência com o padrão já adotado no Hub Core, **este projeto não utiliza uma camada explícita de `infrastructure`**. A persistência de dados ocorre de forma pragmática: as interfaces do Spring Data JPA são estendidas e utilizadas diretamente dentro do pacote `domain/port`.

### Estrutura de Pacotes

```text
src/main/java/com/portal/conecta/mapa_de_sala/
├── module.seat_map/             # Lógica de negócio isolada do Mapa de Sala
│   ├── application/             # Orquestração (Use Cases, Commands, Queries)
│   │   ├── command/
│   │   ├── query/
│   │   └── use_case/
│   ├── domain/                  # Coração do negócio (Entidades, Regras, Portas)
│   │   ├── exception/
│   │   ├── model/
│   │   ├── port/                # Contratos e interfaces JPA (Padrão do Hub)
│   │   └── validator/
│   └── presentation/            # Ponto de entrada (Controllers, DTOs)
│       ├── controller/
│       ├── dto/
│       │   ├── request/
│       │   └── response/
│       └── mapper/
└── shared/                      # Recursos globais e transversais da aplicação
    ├── config/
    ├── context/
    ├── exception/
    └── security/
```
## Banco de dados local (PostgreSQL)

O serviço usa um banco PostgreSQL dedicado (`mapa_sala`). Não compartilha schema com o Hub nem com outros módulos.

## Local Setup

### Requirements

- Java 21
- Maven or Maven Wrapper
- Docker and Docker Compose

### Environment Variables

```
SPRING_PROFILES_ACTIVE=local
SERVER_PORT=8082

DB_HOST=localhost
DB_PORT=5433
DB_NAME=seat_map
DB_USER=seat_map
DB_PASSWORD=alterar

JWT_SECRET=alterar
HUB_API_URL=http://localhost:8080
```

## Passos para Subir o banco local
 
Run PostgreSQL
* ```docker compose up -d postgres```

Check container status:

* ```docker compose ps```
  
Stop (keep data):

* ```docker compose down```

Remove volume:
* ```docker compose down -v```

## Ambientes

* **dev:** ambiente local padrão. Usa H2 em memória para subir sem depender de PostgreSQL.
* **test:** ambiente dos testes automatizados. Usa H2 isolado e recria o schema a cada execução.
* **prod:** ambiente de produção. Usa PostgreSQL configurado por variáveis de ambiente.

---

## Rodar localmente

.\mvnw.cmd spring-boot:run

O profile `dev` é ativado por padrão. A aplicação sobe em `http://localhost:8080`.

**Banco de Dados Local:**
Acesse o console do H2 pelo navegador em `http://localhost:8080/h2-console`.

**Health check:**
```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```