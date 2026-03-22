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
- Opcionalmente: caminho da PRD aprovada em `prds/` — se fornecida, os campos Purpose, usuários e contexto serão derivados do PRD automaticamente, sem pedir que o dev repita informação
- Opcionalmente: quais outros módulos ela toca

---

## Output

### Nível único — Spec completa + Checklist + Plano Técnico

A spec é gerada **de uma só vez** com todos os campos preenchidos e o plano técnico de implementação ao final. O objetivo é que o dev receba um artefato completo para revisar e aprovar — sem necessidade de iterações por nível.

Se alguma informação não puder ser inferida, preencher com a melhor suposição e marcar com `⚠️ suposição — confirmar com o time`.

```markdown
# Spec: [Nome da Feature]

## Status
- [x] Draft
- [ ] Approved
- [ ] Implemented
- [ ] Tested

## Purpose
[Uma frase descrevendo o objetivo.]

> Derivado de: `prds/[nome].prd.md`

---

## Contracts

### Types (Kotlin)
[Data classes, sealed classes, interfaces e enums — apenas assinaturas, sem implementação.]

### Inputs
[O que dispara esta feature.]

### Outputs / Side Effects
[O que ela produz ou causa.]

---

## Behavior

### Happy Path
[Passo a passo numerado do fluxo principal — do início até o resultado final.]

### Error / Edge Cases
[Tabela ou lista de cenários alternativos com origem e comportamento esperado.]

---

## Integration Points

| Módulo | Papel |
|---|---|
| [arquivo ou módulo] | [responsabilidade] |

---

## Out of Scope
[Lista explícita do que NÃO entra nesta entrega — evita scope creep.]

---

## Open Questions
[Dúvidas técnicas ou de produto com contexto — a resolver antes de iniciar a implementação.]

---

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

---

## 📐 Plano Técnico de Implementação

> Sem código — apenas o plano arquitetural para orientar a implementação.

### Arquivos a criar
  feature/[name]/ui/[Name]Screen.kt              ← Screen Composable (UI + eventos)
  feature/[name]/viewmodel/[Name]ViewModel.kt     ← ViewModel (estado + lógica MVI)
  [outros arquivos conforme a feature]

### Arquivos a modificar
  [ex: AndroidManifest.xml — permissões]
  [ex: domain/repository/[Name]Repository.kt — novo contrato]

### Padrões SOLID aplicados
  - S — [Name]Screen lida apenas com UI; [Name]ViewModel lida apenas com estado
  - O — comportamentos variáveis injetados como parâmetros / lambdas, não hardcoded
  - D — ViewModel recebe dependências via Koin — não instancia diretamente

### Ordem de implementação recomendada
  1. [arquivo sem dependências internas — ex: modelos de domínio, enums]
  2. [arquivo que depende do anterior]
  3. [Screen Composable — último, pois depende do ViewModel]

### Próximo passo
  Diga "aprovado" para o agente atualizar o status do arquivo.
  Em seguida, invocar: new-screen / new-composable / new-repository
```

---

## Constraints
- Escrever o arquivo em `specs/[nome-da-feature].spec.md` **imediatamente** ao gerar — não esperar aprovação para criar.
- Criar com status `[x] Draft` e `[ ] Approved`.
- Quando o dev disser explicitamente "aprovado" (ou equivalente), atualizar o arquivo para `[ ] Draft` / `[x] Approved`.
- **Nunca** gerar código de implementação (`.kt`) nesta skill.
- **Nunca** omitir o Checklist de Completude — é obrigatório em toda spec gerada.
- **Nunca** omitir o Plano Técnico de Implementação — é obrigatório em toda spec gerada.
- **Nunca** executar `git add` / `git commit` automaticamente.
- O checklist deve refletir o estado real da spec gerada (✅ para campos preenchidos, ❌ para campos vazios ou insuficientes).
