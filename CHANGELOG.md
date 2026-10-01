# Changelog

Todas as mudanças relevantes deste projeto são documentadas neste arquivo.

O formato segue o [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/)
e o projeto adota o [Versionamento Semântico](https://semver.org/lang/pt-BR/).

Toda alteração (feature, correção, configuração) deve adicionar uma entrada na
seção `[Unreleased]`. Ao gerar uma release (merge de `develop` em `main`), a
seção `[Unreleased]` é convertida na nova versão.

## [Unreleased]

### Added
- Estrutura inicial do repositório: `README.md`, `.gitignore`, `.editorconfig`
  e `CHANGELOG.md`.
- Projeto Spring Boot 4.1.1 com Java 25 (LTS) e Maven Wrapper (Maven 3.9.11).
- Dependências base: Spring Web MVC, Validation e Actuator (endpoints `health` e
  `info`, com probes de liveness/readiness habilitadas).
- Aplicação configurada na porta `8081` (sobrescrevível via `SERVER_PORT`).
- JaCoCo 0.8.15 com gate de cobertura mínima de 80% de linhas na fase `verify`;
  o build falha se a cobertura ficar abaixo do mínimo.
- Testes iniciais: carregamento do contexto e endpoint `/actuator/health`.
- `.gitattributes` para padronizar finais de linha (LF, e CRLF para scripts `.cmd`).
- Pipeline de CI no GitHub Actions (`.github/workflows/ci.yml`): executa build,
  testes e verificação de cobertura mínima de 80% em todo Pull Request e push
  para `develop` e `main`. Publica o resumo de cobertura no job e o relatório
  do JaCoCo como artefato (`jacoco-report`).
- CI também executa em push para branches `feature/**`.
- Job `auto-pull-request` no pipeline: com o build verde, abre automaticamente o
  PR `feature/* -> develop` e, a cada push em `develop`, o PR de release
  `develop -> main` (sem duplicar PRs já abertos). Aceita o secret opcional
  `PR_AUTOMATION_TOKEN`.
- Ruleset `protect-main-develop` (`.github/rulesets/`) para importação no GitHub:
  bloqueia push direto, force push e exclusão de `main`/`develop` e exige PR com
  o check de build/testes/cobertura verde.
- Banco de dados PostgreSQL exclusivo do serviço de vendas: Spring Data JPA,
  driver PostgreSQL e Flyway (migrações em `src/main/resources/db/migration`).
  Conexão configurável pelas variáveis `DB_HOST`, `DB_PORT`, `DB_NAME`,
  `DB_USERNAME` e `DB_PASSWORD`. O Hibernate apenas valida o schema
  (`ddl-auto: validate`); o schema é gerido pelo Flyway.
- `compose.yaml` com PostgreSQL 17 para desenvolvimento local, exposto na porta
  `5433` para não conflitar com o banco do `vehicle-platform-api`.
- Testes de integração com Testcontainers (PostgreSQL 17 real), incluindo teste
  de conexão com o banco.
- Sincronização de veículos com o `vehicle-platform-api`
  (`PUT /internal/vehicles/{id}`): cria o veículo (201) ou atualiza seus dados
  (200) de forma idempotente, usando o mesmo `id` do serviço principal.
  - Veículos são criados com status `AVAILABLE`; veículos `RESERVED` ou `SOLD`
    não podem ser editados (409).
  - Validação de entrada com Bean Validation (400 com a lista de campos
    inválidos) e regras de domínio (422), como ano entre 1900 e o ano seguinte
    ao atual e preço maior que zero.
- Migração `V1__create_vehicles_table`: tabela `vehicles` com controle de
  concorrência otimista (`version`) e índice `(status, price)` para as listagens.
- Arquitetura em camadas: domínio (`domain`), casos de uso e portas
  (`application`) e adaptadores web/JPA (`infrastructure`). Os casos de uso não
  dependem do Spring e são registrados em `UseCaseConfig`.
- Respostas de erro padronizadas no formato Problem Details (RFC 9457).
- Mensagens de validação sempre em português (`spring.web.locale: pt_BR`).
- Listagem de veículos à venda (`GET /vehicles/available`): retorna apenas os
  veículos `AVAILABLE`, ordenados por preço do mais barato para o mais caro
  (com desempate pelo `id`, para paginação estável).
  - Paginação por `page` (padrão `0`) e `size` (padrão `20`, máximo `100`).
  - Resposta com `content`, `page`, `size`, `totalElements` e `totalPages`.
  - Parâmetros inválidos retornam 400 com os detalhes em `errors`.
