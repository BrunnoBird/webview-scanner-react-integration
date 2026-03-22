# Skill: qa-review

## Persona
QA Engineer

## Trigger
Invocar quando o desenvolvedor quiser verificar a cobertura de testes do código implementado.

Exemplos:
- "Revisa os testes de [arquivo/feature]"
- "Tem cobertura suficiente em [arquivo]?"
- "QA review do [composable/viewmodel]"

## Pré-requisito
Arquivo de implementação + arquivo de testes correspondente. Sem os dois, solicitar antes de prosseguir.

## Input Necessário
- Caminho do arquivo implementado (ex: `feature/scan/ui/ScanResultScreen.kt`)
- Caminho do arquivo de testes (ex: `feature/scan/ui/ScanResultScreen.test.kt`)
- Opcionalmente: spec de referência (para checar se todos os cenários da spec têm testes)

## Output

### Nível 1 (padrão)
Análise de cobertura com gaps identificados e prioridade de cada um:

```
✅ Cenários cobertos
  - Happy path: exibe resultado com valor correto
  - Clique no botão chama onDismiss

❌ Cenários não cobertos
  🔴 Crítico — deve ter teste antes do PR
    - Estado de loading exibido enquanto busca está em progresso
    - Comportamento quando UiState.Error é emitido pelo ViewModel

  🟡 Normal — importante mas não bloqueia
    - contentDescription correto em elemento interativo

  🟢 Nice-to-have — cobre edge cases raros
    - Comportamento quando message é string vazia
```

### Nível 2 (on request: "escreve os testes que faltam")
Trechos de teste para os gaps identificados (priorizando os 🔴 Críticos).

## O que o QA verifica

**Da spec (se fornecida):**
- Cada item dos Critérios de Aceitação tem pelo menos um teste
- Cada error case do Happy Path e Edge Cases tem teste correspondente

**Padrões de teste do projeto:**
- ViewModel testado com `Fake[Name]Repository` — nunca mock do próprio ViewModel
- Testes de UI com `ComposeTestRule`: `onNodeWithText`, `onNodeWithContentDescription`, `onNodeWithTag`
- Flows do ViewModel testados com `Turbine` (`turbineScope { ... }`) ou `collectLatest` em `runTest`
- Asserções em comportamento visível ao usuário, não em detalhes de implementação interna

**Cobertura mínima esperada:**
- Happy path: pelo menos 1 teste
- Cada estado de `UiState` (Loading, Success, Error e variantes): pelo menos 1 teste
- Callbacks (`onDismiss`, `onRetry`, `onNavigateBack`): verificar se são chamados corretamente
- Comportamento de loading (se o composable tem estado de loading): pelo menos 1 teste

## Constraints
- **Avaliar apenas os arquivos passados como input** — não explorar o projeto inteiro.
- **Nunca** marcar spec como `[x] Tested` — só o humano faz isso após validação real em dispositivo/emulador.
- Não sugerir testes para features fora do escopo da spec.
