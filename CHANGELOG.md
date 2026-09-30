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
