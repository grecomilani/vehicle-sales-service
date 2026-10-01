# Vehicle Sales Service

[![CI](https://github.com/grecomilani/vehicle-sales-service/actions/workflows/ci.yml/badge.svg?branch=develop)](https://github.com/grecomilani/vehicle-sales-service/actions/workflows/ci.yml)

Serviço de **venda de veículos** da plataforma de revenda de veículos automotores,
desenvolvido como Trabalho Substitutivo do Tech Challenge — Fase 4 (Pós-Tech SOAT).

## O que é o projeto

A plataforma é composta por dois serviços que se comunicam via HTTP, cada um com
seu próprio banco de dados:

| Serviço | Responsabilidade |
|---|---|
| [`vehicle-platform-api`](https://github.com/grecomilani/vehicle-platform-api) | Cadastro e edição de veículos, webhook de pagamento |
| **`vehicle-sales-service`** (este repositório) | Listagem de veículos à venda e vendidos, compra de veículos e atualização do status de pagamento |

O serviço de vendas é isolado para suportar picos de acesso nos endpoints de
listagem e compra, e usa um banco de dados segregado.

## Stack

- Java 25 (LTS)
- Spring Boot
- Maven
- PostgreSQL 17 (banco exclusivo deste serviço) + Flyway
- JUnit 5 + Testcontainers + JaCoCo (cobertura mínima de 80%)

## Fluxo de trabalho (Git Flow)

- `main`: código em produção. O deploy é disparado pelo merge nesta branch.
- `develop`: branch de integração.
- `feature/<nome>`: criada a partir de `develop` e integrada via Pull Request.

Toda alteração deve ser registrada no [CHANGELOG.md](CHANGELOG.md).

## CI/CD

O workflow [`ci.yml`](.github/workflows/ci.yml) roda em todo Pull Request para
`develop`/`main` e em todo push para `feature/**`, `develop` e `main`:

1. Build do projeto com JDK 25.
2. Execução dos testes automatizados.
3. Verificação da cobertura mínima de **80%** (JaCoCo). O pipeline falha se
   a cobertura ficar abaixo do mínimo.
4. Publicação do resumo de cobertura no job e do relatório completo como
   artefato (`jacoco-report`).
5. **Pull Request automático** (job `auto-pull-request`), quando o build está verde:
   - push em `feature/*` abre o PR para `develop`;
   - push em `develop` abre o PR de release para `main`.

   Se já houver um PR aberto para o mesmo par de branches, nada é feito.

### Configuração necessária no GitHub

- **Permitir que o Actions crie PRs:** *Settings → Actions → General → Workflow
  permissions* → marcar *Allow GitHub Actions to create and approve pull requests*.
  Alternativa: criar o secret `PR_AUTOMATION_TOKEN` com um token (PAT) que tenha
  permissão de *Pull requests: write*.
- **Proteção de `main` e `develop`:** *Settings → Rules → Rulesets → New ruleset →
  Import a ruleset* e selecionar
  [`.github/rulesets/protect-main-develop.json`](.github/rulesets/protect-main-develop.json).
  O ruleset bloqueia push direto, force push e exclusão das branches e exige PR
  com o check *Build, testes e cobertura (mínimo 80%)* verde.

## Arquitetura

O código segue uma arquitetura em camadas, com as dependências apontando para o
domínio:

```
domain/          Regras de negócio (Vehicle, VehicleStatus, exceções)
application/     Casos de uso e portas (interfaces), sem dependência do Spring
infrastructure/  Adaptadores: web (controllers), persistência (JPA) e configuração
```

## API

### Endpoints públicos (consumidos pelo frontend)

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/vehicles/available?page=0&size=20` | Veículos à venda, do mais barato para o mais caro |

A listagem é paginada: `page` começa em `0`, e `size` tem padrão `20` e máximo
`100`. Exemplo de resposta:

```json
{
  "content": [
    {"id": "3f1c2a4e-...", "brand": "Fiat", "model": "Uno", "year": 2015,
     "color": "Branco", "price": 30000.00, "status": "AVAILABLE"}
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1
}
```

### Endpoints internos (chamados pelo `vehicle-platform-api`)

| Método | Rota | Descrição |
|---|---|---|
| `PUT` | `/internal/vehicles/{id}` | Cria ou atualiza um veículo (sincronização do cadastro) |

Exemplo:

```bash
curl -X PUT http://localhost:8081/internal/vehicles/3f1c2a4e-8b7d-4c1a-9e2f-1a2b3c4d5e6f \
  -H 'Content-Type: application/json' \
  -d '{"brand":"Toyota","model":"Corolla","year":2022,"color":"Prata","price":85000.00}'
```

| Status | Quando |
|---|---|
| `201 Created` | Veículo criado |
| `200 OK` | Veículo existente atualizado |
| `400 Bad Request` | Campos inválidos (a resposta lista os campos em `errors`) |
| `409 Conflict` | Veículo reservado ou vendido não pode ser alterado |
| `422 Unprocessable Content` | Regra de negócio violada (ex.: ano fora do intervalo permitido) |

Os erros seguem o formato [Problem Details (RFC 9457)](https://www.rfc-editor.org/rfc/rfc9457).

## Como executar localmente

Pré-requisitos: JDK 25 e Docker. Não é preciso instalar o Maven: o projeto usa o
Maven Wrapper.

1. Suba o banco de dados do serviço (PostgreSQL na porta `5433`):

   ```bash
   docker compose up -d
   ```

2. Inicie a aplicação:

   ```bash
   ./mvnw spring-boot:run
   ```

As migrações do Flyway são aplicadas automaticamente na inicialização.
A aplicação sobe na porta `8081` (altere com a variável `SERVER_PORT`).

| Variável | Padrão | Descrição |
|---|---|---|
| `SERVER_PORT` | `8081` | Porta HTTP da aplicação |
| `DB_HOST` | `localhost` | Host do PostgreSQL |
| `DB_PORT` | `5433` | Porta do PostgreSQL |
| `DB_NAME` | `vehicle_sales` | Nome do banco |
| `DB_USERNAME` | `vehicle_sales` | Usuário do banco |
| `DB_PASSWORD` | `vehicle_sales` | Senha do banco |

Para verificar se ela está no ar:

```bash
curl http://localhost:8081/actuator/health
```

## Como testar

```bash
./mvnw verify
```

Os testes de integração usam **Testcontainers** para subir um PostgreSQL real,
então o Docker precisa estar em execução.

O comando executa os testes e gera o relatório de cobertura do JaCoCo em
`target/site/jacoco/index.html`. O build **falha** se a cobertura de linhas
ficar abaixo de **80%**.
