# Skill: new-repository

## Persona
Desenvolvedor

## Trigger
Invocar quando houver uma spec de **camada de dados** com status `[x] Approved` e o time precisar gerar o contrato de domínio e a implementação.

Exemplos:
- "Gera o repository de pagamento"
- "Cria o contrato de dados para o histórico de scan"
- "A spec do ScanRepository foi aprovada, gera os arquivos"

## Pré-requisito Obrigatório
Spec aprovada em `specs/` com `[x] Approved`. Se não aprovada, **não executar**.

## Input Necessário
- Caminho da spec aprovada
- Nome do repository (ex: `Scan`, `Payment`, `History`)

## Output

### Nível 1 (padrão)
Apenas a **estrutura de arquivos** a criar:

```
Arquivos a criar:
  domain/model/[Name].kt                    ← entidade de domínio
  domain/repository/[Name]Repository.kt     ← interface (contrato)
  data/repository/[Name]RepositoryImpl.kt   ← implementação
  data/fake/Fake[Name]Repository.kt         ← fake para testes

Próximo passo: peça "nível 2" para ver o código de cada arquivo.
```

### Nível 2 (on request: "gera os arquivos")
Código completo dos 4 arquivos:

**`domain/model/[Name].kt`** — Data classes Kotlin derivadas diretamente da seção "Contracts" da spec. Sem lógica de negócio, sem dependências de framework.

**`domain/repository/[Name]Repository.kt`** — Interface no módulo de domínio. Métodos tipados com `suspend fun` ou `Flow` conforme definido na spec. Sem importações de `android.*` ou `data.*`.

**`data/repository/[Name]RepositoryImpl.kt`** — Implementação anotada com `@Inject constructor`. Depende de DataSources (também injetados). Mapeia erros para tipos do domínio.

**`data/fake/Fake[Name]Repository.kt`** — Fake que implementa a interface de domínio. Usa dados realistas. Sem delays artificiais (controlado por testes). Permite configurar respostas via propriedade pública para facilitar cenários de teste.

### Nível 3 (on request: "documenta a estratégia de Coroutine")
Explica as 3 opções de contrato para o método principal, com trade-offs:

1. **`suspend fun`** — retorna resultado único; ideal para operações pontuais (busca, submissão)
2. **`Flow<T>`** — emite múltiplos valores; ideal para observação contínua (cache + remoto, polling)
3. **`Flow<Result<T>>`** — combina reatividade com tratamento explícito de erros

## Princípios do Desenvolvedor

O código gerado aplica SOLID no contexto Clean Architecture Android:
- **S:** `domain/model` define apenas entidades; `domain/repository` define apenas contratos; `data/repository` implementa apenas transporte de dados.
- **I:** Interface do repository expõe apenas os métodos descritos na spec — sem métodos genéricos "para uso futuro".
- **D:** ViewModel depende da interface de domínio, não da implementação — Hilt injeta `[Name]RepositoryImpl` onde `[Name]Repository` é declarado.

Adicionalmente:
- Nomes de variáveis/funções em inglês.
- Sem `Any` — toda tipagem derivada da spec.

## Constraints
- **Nunca** implementar lógica de negócio no repository — apenas transporte e mapeamento de dados.
- **Nunca** importar classes `android.*` na camada `domain/` — ela deve ser pura Kotlin.
- O Fake **deve** implementar a mesma interface que o repository real — sem duck typing.
- A interface em `domain/repository/` é o único contrato que o ViewModel conhece — não importa de `data/` diretamente.

## Estrutura de Arquivos Padrão

```
domain/
  model/
    [Name].kt                     ← entidade de domínio (data class puro)
  repository/
    [Name]Repository.kt           ← interface (contrato para o ViewModel)
data/
  repository/
    [Name]RepositoryImpl.kt       ← implementação (Hilt @Inject)
  fake/
    Fake[Name]Repository.kt       ← fake para testes unitários
```
