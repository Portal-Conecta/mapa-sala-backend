# Portal Conecta - Mapa de Sala Backend

API e lógica de negócio do Mapa de Sala.

**Pré-requisitos:** Java 21 e Maven (via wrapper `mvnw` incluído no projeto).

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