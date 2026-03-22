# Skill: new-spec

## Persona
Arquiteto de Software

## Trigger
Invocar quando o desenvolvedor descrever uma nova feature ou comportamento que ainda não tem spec.

Exemplos:
- "Quero criar uma tela de confirmação de pagamento"
- "Preciso de um repositório para buscar o histórico de transações"
- "Adicionar tela de erro quando o scan falhar"
- "Gera spec para este PRD"

## Input Necessário
- Descrição da feature (o que ela faz, quem a usa, quando é disparada)
- Opcionalmente: caminho da PRD aprovada em `prds/` — se fornecida, os campos Purpose, Usuários e contexto serão derivados do PRD automaticamente, sem pedir que o dev repita informação
- Opcionalmente: quais outros módulos ela toca

## Output

### Nível 1 (padrão — sempre retornar isso primeiro)
Esqueleto da spec com **apenas os campos obrigatórios preenchidos**. Seções de behavior, edge cases e open questions ficam vazias para o desenvolvedor preencher/validar.

```markdown
# Spec: [Nome da Feature]

## Status
- [x] Draft
- [ ] Approved
- [ ] Implemented
- [ ] Tested

## Purpose
[Uma frase descrevendo o objetivo.]

## Contracts

### Types
[Data classes, sealed classes e interfaces Kotlin — apenas assinaturas, sem implementação.]

### Inputs
[O que dispara esta feature.]

### Outputs / Side Effects
[O que ela produz ou causa.]

## Behavior

### Happy Path
[Deixar em branco para o dev preencher]

### Error / Edge Cases
[Deixar em branco para o dev preencher]

## Integration Points
[Lista de módulos tocados]

## Out of Scope
[Deixar em branco para o dev preencher]

## Open Questions
[Deixar em branco para o dev preencher]
```

### Nível 2 (on request: "preenche a spec")
Spec completamente preenchida com comportamento, edge cases e open questions sugeridas.

Ao final da spec, incluir obrigatoriamente a seção de checklist:

```markdown
## ✅ Checklist de Completude

✅ / ❌  Purpose em uma frase clara
✅ / ❌  Contracts com data classes / sealed classes Kotlin
✅ / ❌  Inputs definidos
✅ / ❌  Outputs / Side Effects definidos
✅ / ❌  Happy Path descrito passo a passo
✅ / ❌  Error / Edge Cases listados
✅ / ❌  Integration Points mapeados
✅ / ❌  Out of Scope explícito
✅ / ❌  Open Questions resolvidas ou documentadas

[Para specs de Repository/DataSource, verificar também:]
✅ / ❌  Estratégia de Coroutine (suspend vs Flow) definida
✅ / ❌  Comportamento de erro (sealed Result / Exception) definido
✅ / ❌  Escopo do Coroutine (viewModelScope, lifecycleScope) endereçado

**Resultado: PRONTA PARA APROVAÇÃO / PRECISA DE REVISÃO**
```

### Nível 3 (on request: "plano técnico")
Plano de implementação técnica derivado da spec. Sem código — apenas o plano arquitetural com princípios SOLID aplicados.

```
Arquivos a criar:
  feature/[name]/ui/[Name]Screen.kt          ← Screen Composable (UI + eventos)
  feature/[name]/viewmodel/[Name]ViewModel.kt ← ViewModel (estado + lógica)
  feature/[name]/ui/[Name]Screen.test.kt      ← Compose UI tests

Arquivos a modificar:
  domain/repository/[Name]Repository.kt       ← interface a adicionar/ajustar

Padrões SOLID aplicados:
  - S — [Name]Screen lida apenas com UI; [Name]ViewModel lida apenas com estado
  - D — ViewModel recebe [Name]Repository via Hilt — não instancia

Ordem de implementação recomendada:
  1. [arquivo sem dependências internas]
  2. [arquivo que depende do anterior]

Próximo passo: invoke new-screen / new-composable / new-repository
```

## Constraints
- **Nunca** gerar código de implementação (`.kt`) nesta skill.
- **Nunca** marcar status como `Approved` — isso é papel do desenvolvedor.
- **Nunca** pular o checklist no L2 — é obrigatório ao final de toda spec preenchida.
- O checklist deve refletir o estado real da spec gerada (✅ para campos preenchidos, ❌ para campos vazios ou insuficientes).
