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
- PostgreSQL
- JUnit 5 + JaCoCo (cobertura mínima de 80%)

## Fluxo de trabalho (Git Flow)

- `main`: código em produção. O deploy é disparado pelo merge nesta branch.
- `develop`: branch de integração.
- `feature/<nome>`: criada a partir de `develop` e integrada via Pull Request.

Toda alteração deve ser registrada no [CHANGELOG.md](CHANGELOG.md).

## CI/CD

O workflow [`ci.yml`](.github/workflows/ci.yml) roda em todo Pull Request e push
para `develop` e `main`:

1. Build do projeto com JDK 25.
2. Execução dos testes automatizados.
3. Verificação da cobertura mínima de **80%** (JaCoCo). O pipeline falha se
   a cobertura ficar abaixo do mínimo.
4. Publicação do resumo de cobertura no job e do relatório completo como
   artefato (`jacoco-report`).

## Como executar localmente

Pré-requisito: JDK 25. Não é preciso instalar o Maven: o projeto usa o Maven Wrapper.

```bash
./mvnw spring-boot:run
```

A aplicação sobe na porta `8081` (altere com a variável `SERVER_PORT`).
Para verificar se ela está no ar:

```bash
curl http://localhost:8081/actuator/health
```

## Como testar

```bash
./mvnw verify
```

O comando executa os testes e gera o relatório de cobertura do JaCoCo em
`target/site/jacoco/index.html`. O build **falha** se a cobertura de linhas
ficar abaixo de **80%**.
