# Changelog

Todas as mudanças relevantes deste projeto são documentadas neste arquivo.

O formato segue o [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/)
e o projeto adota o [Versionamento Semântico](https://semver.org/lang/pt-BR/).

Toda alteração (feature, correção, configuração) deve adicionar uma entrada na
seção `[Unreleased]`. Ao gerar uma release (merge de `develop` em `main`), a
seção `[Unreleased]` é convertida na nova versão.

## [Unreleased]

### Security
- Pipeline: os downloads do kubeconform (CI) e do kind (CD) passam a exigir HTTPS também nos redirecionamentos (`--proto '=https'`) e a conferir o SHA-256 do binário antes de usá-lo. Corrige a vulnerabilidade apontada pelo SonarQube Cloud.

### Changed
- Pipeline: o job `auto-pull-request` só abre o PR (`feature/*` → `develop` e `develop` → `main`) depois que todos os jobs de verificação passam e não faz mais merge automático; o merge passa a ser manual. A opção *Allow auto-merge* do repositório deixa de ser necessária.

### Added
- Análise de qualidade no SonarQube Cloud (organização `grecomilani`, projeto `grecomilani_vehicle-sales-service`) no job de build do CI, com a cobertura do JaCoCo (`target/site/jacoco/jacoco.xml`). Roda em pull requests e nos pushes em `develop`/`main`; exige o secret `SONAR_TOKEN`.

### Documentation
- Adicionados diagramas Mermaid em `docs/architecture.md` e `docs/flow.md` para infraestrutura Kubernetes, componentes das APIs e sequência ponta a ponta com pagamentos; links publicados no README.

### Documentation
- README: diagrama Mermaid dos microsserviços e bancos segregados, fluxo de webhook com cenários PAID/CANCELED e idempotência, evidências de CI/CD e explicação do cluster kind efêmero.
- Esclarece a branch de release e as limitações do deploy; aprimora orientações de execução e integração HTTP.


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
- Compra de veículo (`POST /sales`) com CPF do comprador e data da venda:
  reserva o veículo (`RESERVED`) e cria a venda com pagamento `PENDING`, na
  mesma transação. A resposta traz o `paymentCode`, usado pela entidade
  processadora de pagamentos para informar o resultado.
  - CPF validado (dígitos verificadores), aceito com ou sem máscara e
    armazenado só com os dígitos.
  - A data da venda não pode estar no futuro (fuso configurável por
    `APP_TIME_ZONE`, padrão `America/Sao_Paulo`).
  - O preço do veículo no momento da compra é registrado na venda.
  - Respostas: 201 (venda criada), 400 (dados inválidos), 404 (veículo não
    encontrado), 409 (veículo reservado/vendido ou compra simultânea),
    422 (data no futuro).
- Proteção contra compras simultâneas do mesmo veículo: controle de
  concorrência otimista (`version`) no veículo e índice único parcial
  `uq_sales_active_vehicle`, que permite no máximo uma venda ativa
  (pendente ou paga) por veículo.
- Migração `V2__create_sales_table`: tabela `sales`.
- Porta `TransactionRunner`, para executar casos de uso em transação sem
  acoplar a camada de aplicação ao Spring.
- Atualização do status de pagamento
  (`PATCH /internal/sales/{paymentCode}/payment`), chamada pelo
  `vehicle-platform-api` quando o webhook de pagamento é recebido:
  - `PAID`: a venda fica paga e o veículo passa a `SOLD`.
  - `CANCELED`: a venda é cancelada e o veículo volta a `AVAILABLE`, podendo
    ser comprado novamente.
  - Idempotente: reenviar o mesmo resultado retorna 200 sem alterar nada.
  - Um pagamento já finalizado não muda para o outro resultado (409); código
    de pagamento inexistente retorna 404; status diferente de `PAID`/`CANCELED`
    retorna 400.
- `ConflictException` no domínio, mapeada para 409 no Problem Details.
- Listagem de veículos vendidos (`GET /vehicles/sold`): retorna apenas os
  veículos `SOLD` (pagamento confirmado), ordenados por preço do mais barato
  para o mais caro, com a mesma paginação da listagem de veículos à venda.
- Documentação da API com OpenAPI 3 (springdoc-openapi 3.1.1): Swagger UI em
  `/swagger-ui.html` e especificação em `/v3/api-docs`, com os endpoints
  agrupados por assunto e descritos.
- `Dockerfile` multi-stage: build com o Maven Wrapper (JDK 25) e runtime com a
  JRE 25, em camadas do Spring Boot, executando como usuário não-root e com a
  memória da JVM limitada à do container (`MaxRAMPercentage=75`). Aceita o
  build-arg opcional `MAVEN_MIRROR_URL`.
- Manifests Kubernetes em `k8s/` (Kustomize), no namespace `revenda-veiculos`:
  - `database.yaml`: PostgreSQL exclusivo do serviço (Secret, Service e
    StatefulSet com volume persistente).
  - `configmap.yaml`, `deployment.yaml` (2 réplicas, rolling update sem
    indisponibilidade, probes de startup/liveness/readiness, limites de recursos
    e execução não-root) e `service.yaml`.
  - `hpa.yaml`: HorizontalPodAutoscaler de 2 a 6 réplicas por uso de CPU, para
    suportar picos de acesso nas listagens e compras.
- Pipeline de deploy contínuo (`.github/workflows/deploy.yml`), executado a
  cada merge na `main`: publica a imagem no GitHub Container Registry, cria um
  cluster Kubernetes com kind no runner (com metrics-server), implanta os
  manifests com a imagem do commit, aguarda o rollout e executa um smoke test
  ponta a ponta (`.github/scripts/smoke-test.sh`). Em caso de falha, imprime o
  diagnóstico do cluster.

### Changed
- Pipeline: o job `auto-pull-request` passa a habilitar o merge automático dos
  PRs que abre (`feature/* -> develop` e `develop -> main`). O merge acontece
  quando os checks obrigatórios ficam verdes, e o PR pode ser segurado com a
  label `hold`. O fluxo completo exige o secret `PR_AUTOMATION_TOKEN` e a opção
  *Allow auto-merge* do repositório.
- Pipeline: novo job `docker-image` valida o build da imagem Docker em todo push
  e PR; o merge automático passa a depender dele.
- Pipeline: novo job `k8s-manifests` renderiza os manifests com Kustomize e os
  valida contra os schemas do Kubernetes (kubeconform); o merge automático passa
  a depender dele.
- Imagem Docker passa a usar UID/GID numéricos (`10001`), permitindo ao
  Kubernetes verificar que o container não roda como root.
- Deployment: `runAsUser`/`runAsGroup` explícitos, sem escalada de privilégio e
  sem capabilities; request de CPU reduzido para `100m` (sem limite de CPU, a JVM
  continua podendo usar mais no startup).
- Deployment: `initContainer` aguarda o PostgreSQL aceitar conexões antes de
  iniciar a aplicação, evitando reinícios por falha do Flyway na inicialização.

### Fixed
- Pipeline: o merge automático falhava quando o check do próprio PR ainda
  estava sendo registrado logo após a criação do PR (o GitHub recusava o merge
  com o check `queued`). O job agora tenta novamente a cada 20s, por até 10
  minutos, até agendar o auto-merge ou concluir o merge.
