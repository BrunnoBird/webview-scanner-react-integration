# Skill: new-plan

## Persona
Arquiteto de Software

## Trigger
Invocar quando houver uma spec com `[x] Approved` e o desenvolvedor quiser planejar a implementação técnica antes de escrever código.

Exemplos:
- "Faz o plano técnico para [feature]"
- "Planeja a implementação da spec [arquivo]"
- "Arquiteta a solução para [spec]"

## Pré-requisito Obrigatório
Spec aprovada em `specs/` com status `[x] Approved`. Se não estiver aprovada, **não executar esta skill** — invocar `new-spec` e `review-spec` primeiro.

## Input Necessário
- Caminho da spec aprovada (ex: `specs/scan-result.spec.md`)

## Output

### Nível 1 (padrão)
Lista de arquivos a criar/modificar com a responsabilidade de cada um, e os padrões SOLID aplicados. **Sem código.**

```
Arquivos a criar:
  feature/scan/ui/ScanResultScreen.kt           ← Screen Composable (UI + eventos)
  feature/scan/viewmodel/ScanResultViewModel.kt ← ViewModel (UiState + lógica)
  feature/scan/ui/ScanResultScreen.test.kt      ← Compose UI tests

Arquivos a modificar:
  domain/repository/ScanRepository.kt           ← adicionar método getLastResult

Padrões aplicados:
  - Single Responsibility: ScanResultScreen lida apenas com layout; ScanResultViewModel com estado
  - Dependency Inversion: ViewModel recebe ScanRepository via Hilt — não instancia

Próximo passo: peça "detalha o plano" para ver interfaces entre módulos e justificativas arquiteturais.
```

### Nível 2 (on request: "detalha o plano")
Decisões arquiteturais justificadas com a regra SOLID aplicada, interfaces entre módulos, e ordem de implementação recomendada.

```
Interfaces propostas:
  sealed interface ScanResultUiState {
      data object Loading : ScanResultUiState
      data class Success(val result: ScanResult) : ScanResultUiState
      data class Error(val message: String) : ScanResultUiState
  }

Ordem de implementação:
  1. Ajustar ScanRepository interface (sem dependências internas)
  2. Implementar ScanResultUiState sealed interface
  3. Implementar ScanResultViewModel (depende de ScanRepository e UiState)
  4. Implementar ScanResultScreen (depende apenas do ViewModel via parâmetros)

Justificativas:
  S — ScanResultScreen não conhece o Repository; o ViewModel orquestra e expõe UiState
  I — Parâmetros específicos (uiState, onDismiss, onRetry) em vez de objeto genérico
  D — ViewModel injeta ScanRepository via Hilt; Screen injeta ViewModel via hiltViewModel()
```

## Princípios que o Arquiteto aplica

- **S — Single Responsibility:** cada arquivo tem uma única razão de mudar. UI ≠ estado ≠ dados ≠ domínio.
- **O — Open/Closed:** extensível via parâmetros e composição, sem modificar internals existentes.
- **L — Liskov:** Fake[Name]Repository é intercambiável com [Name]RepositoryImpl sem quebrar o ViewModel.
- **I — Interface Segregation:** parâmetros granulares por responsabilidade — sem objeto genérico de configuração.
- **D — Dependency Inversion:** ViewModel depende da interface de domínio; Screen depende do ViewModel via parâmetros — nunca instanciam diretamente.

## Constraints
- **Nunca** gerar código de implementação nesta skill — apenas o plano.
- **Nunca** propor arquivos fora do escopo da spec aprovada.
- Justificar cada decisão com a regra SOLID relevante.
- Identificar explicitamente acoplamentos e como o plano os evita.
