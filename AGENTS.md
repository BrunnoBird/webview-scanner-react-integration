# [Nome do Projeto] — Instruções do Agente

## Contexto do Projeto

Aplicativo Android Nativo desenvolvido com Kotlin e Jetpack Compose.

| Item | Valor |
|---|---|
| Stack | Kotlin, Jetpack Compose, Android SDK |
| Arquitetura | MVVM + Clean Architecture (UI / Domain / Data) |
| DI | Hilt |
| Async | Coroutines + Flow |
| Testes | JUnit4, MockK, Compose UI Test, Turbine |
| Idioma da UI | Português Brasileiro |

---

## Metodologia: Spec Driven Development (SDD)

Este projeto usa SDD. Toda feature começa com uma spec aprovada, **nunca com código**.

### Regra de Ouro
> Não existe arquivo de implementação (`.kt`) sem spec em `specs/` com status `[x] Approved`.

### Fluxo obrigatório
```
0. new-prd     → PRD da task (dev revisa e aprova)
1. new-spec    → spec + checklist + plano técnico em 3 níveis:
                   L1: esqueleto
                   L2: spec completa + checklist de completude inline
                   L3: plano técnico (arquivos + SOLID, sem código)
2. Humano aprova spec → muda status para [x] Approved
3. new-screen / new-composable / new-repository → implementação (L1: tipos, L2: código)
4. review-code → code review (🔴 bloqueadores / 🟡 melhorias / 🟢 sugestões)
5. qa-review   → cobertura de testes (✅ cobertos / ❌ gaps) → PR
```

### Status de uma spec
```
- [x] Draft       → em elaboração
- [x] Approved    → aprovado pelo time
- [x] Implemented → código criado e revisado
- [x] Tested      → validado em dispositivo/emulador real
```

---

## Progressive Disclosure — Regra de Resposta

Toda resposta começa no **Nível 1**. Avançar só quando o desenvolvedor pedir.

| Nível | O que entregar |
|---|---|
| 1 (padrão) | Esqueleto / estrutura / tipos / lista de arquivos a criar |
| 2 (`"implementa"` / `"nível 2"`) | Implementação completa seguindo a spec |
| 3 (`"edge cases"` / `"nível 3"`) | Variantes, tratamentos de erro expandidos, notas de integração |

**Nunca pular níveis.** O objetivo é forçar revisão em cada etapa.

---

## Regras Android (não negociáveis)

- Sem lógica de negócio em Composables — apenas UI e disparo de eventos.
- Estado sempre gerenciado via `ViewModel` exposto como `StateFlow<UiState>` imutável.
- DI via Koin — nunca instanciar dependências diretamente em ViewModel ou Composable.
- `koinViewModel()` apenas em Screen Composables — nunca em Composables reutilizáveis.
- Acesso a Repository/DataSource **apenas** dentro de ViewModel ou UseCase — nunca em Composables.
- Sem `Any` sem tipagem explícita — toda tipagem derivada dos contratos da spec.
- Textos em Português Brasileiro em toda a UI.
- Utilizar arquitetura MVI - Seguindo o Flow de Intent, Effect, e Estado unico por tela.
- Sempre prefira o uso da Inversão de Dependencia para facilitacao dos testes unitarios.

---

## Skills Disponíveis

As skills são templates de prompt em `ai/skills/`. Invoque dizendo o que quer:

**Fluxo principal:**

| O que dizer | Arquivo | Persona | Resultado |
|---|---|---|---|
| `"quero uma PRD para [task]"` | `ai/skills/new-prd.md` | Product Manager | PRD completa em `prds/` |
| `"gera spec para este PRD"` / `"cria spec para [feature]"` | `ai/skills/new-spec.md` | Arquiteto | L1: esqueleto · L2: spec + checklist · L3: plano técnico |
| `"implemente a tela [nome]"` | `ai/skills/new-screen.md` | Desenvolvedor | Screen Composable + estrutura |
| `"cria o composable [nome]"` | `ai/skills/new-composable.md` | Desenvolvedor | Composable tipado (SOLID) |
| `"gera repository de [nome]"` | `ai/skills/new-repository.md` | Desenvolvedor | interface + impl + fake |
| `"revisa o código de [arquivo]"` | `ai/skills/review-code.md` | Revisor Sênior | 🔴🟡🟢 pontos categorizados |
| `"QA review do [arquivo]"` | `ai/skills/qa-review.md` | QA Engineer | ✅❌ gaps de cobertura |

**Skills avulsas** (uso opcional, fora do fluxo principal):

| O que dizer | Arquivo | Quando usar |
|---|---|---|
| `"revisa o PRD de [arquivo]"` | `ai/skills/review-prd.md` | Checar PRD existente isoladamente |
| `"revisa spec de [arquivo]"` | `ai/skills/review-spec.md` | Checar spec existente isoladamente |
| `"planeja a implementação da spec [arquivo]"` | `ai/skills/new-plan.md` | Detalhar plano técnico separado da spec |

Leia `ai/docs/flow.md` para o fluxo completo de uma feature com IA, incluindo exemplo real.

---

## Estrutura de Pastas

```
prds/               ← PRDs aprovadas das tasks do board
specs/              ← fonte da verdade de features (SDD)
ai/
  skills/           ← templates de prompt por capacidade
  docs/flow.md      ← documentação do fluxo de IA (com exemplo real)
app/src/main/
  feature/[name]/
    ui/             ← Screen Composables + Composables reutilizáveis
    viewmodel/      ← ViewModels
  domain/
    model/          ← entidades de domínio
    repository/     ← interfaces de Repository
    usecase/        ← Use Cases (opcional)
  data/
    repository/     ← implementações de Repository
    source/         ← DataSources (local/remote)
    fake/           ← Fakes para testes
```

---

## O que Este Agente Nunca Faz

1. Gera código de implementação sem spec aprovada.
2. Marca PRD, spec ou testes como `Approved` / `Tested` — isso é papel do desenvolvedor.
3. Adiciona features além do escopo da spec.
4. Usa `koinViewModel()` fora de Screen Composables.
5. Cria abstrações para uso único.
6. Avança de nível sem ser solicitado.
7. Usa `Any` sem tipagem explícita.
8. Avança da PRD para spec sem aprovação explícita do dev.
