# AI Flow: Como Usar a IA Neste Projeto

## Visão Geral

Este projeto usa **Spec Driven Development (SDD)** com suporte de IA. A IA atua com **personas especializadas** em cada etapa — Product Manager, Arquiteto, Desenvolvedor, Revisor e QA — para garantir qualidade em todas as fases, do requisito ao código.

Na **geração de spec**, o agente entrega tudo de uma vez (spec + checklist + plano técnico) para revisão e aprovação. Na **implementação**, segue o princípio de **progressive disclosure**: começa com a estrutura (L1) e avança para o código completo (L2) quando solicitado.

---

## O Fluxo Completo de uma Feature

```
┌─────────────────────────────────────────────────────────────┐
│  1. TASK: Dev cola a task do board                         │
│     "Quero uma PRD para [task]"                            │
└────────────────────────┬────────────────────────────────────┘
                         │ skill: new-prd (Product Manager)
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  2. PRD CRIADA EM DISCO (`prds/`): Contexto, user stories  │
│     Dev abre o arquivo localmente, revisa e ajusta.        │
│     Para aprovar: edita o arquivo OU diz "aprovado"        │
└────────────────────────┬────────────────────────────────────┘
                         │ agente atualiza status → [ ] Draft / [x] Approved
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  3. SPEC COMPLETA: "Gera spec para este PRD"               │
│     skill: new-spec (Arquiteto) — nível único              │
│     → spec preenchida (behavior, edge cases, contracts)    │
│     → checklist de completude inline                       │
│     → plano técnico (arquivos + SOLID, sem código)         │
│     → task breakdown (grupos paralelos + dependências)     │
└────────────────────────┬────────────────────────────────────┘
                         │ dev edita arquivo spec OU diz "aprovado"
                         │ → agente atualiza spec: [ ] Draft / [x] Approved
                         │ → agente cria: tasks/[nome].tasks.md com o task breakdown
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  4. IMPLEMENTAÇÃO L1:                                      │
│     skill: new-screen / new-composable / new-repository    │
│     (Desenvolvedor — SOLID) — tipos + assinaturas          │
└────────────────────────┬────────────────────────────────────┘
                         │ dev pede "implementa"
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  5. IMPLEMENTAÇÃO L2: Código completo (SOLID, sem Any)     │
└────────────────────────┬────────────────────────────────────┘
                         │ dev pede "revisa o código"
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  6. CODE REVIEW: skill: review-code (Revisor Sênior)       │
│     🔴 Bloqueadores  🟡 Melhorias  🟢 Sugestões            │
└────────────────────────┬────────────────────────────────────┘
                         │ dev corrige, pede "QA review"
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  7. QA REVIEW + PR: skill: qa-review (QA Engineer)         │
│     ✅ Cobertos  ❌ Gaps → dev adiciona testes → PR         │
└─────────────────────────────────────────────────────────────┘
```

---

## Fluxo Específico: Nova Camada de Dados (Repository)

Para qualquer integração com fonte de dados (API, banco local, sensor nativo), o fluxo tem alinhamento extra:

```
1. Dev → new-prd → PRD do repositório/feature
2. Dev aprova PRD → new-spec → spec completa do Repository (behavior + checklist + plano técnico)
3. Dev + Time (backend/infra) → revisam spec + contrato → aprovam
4. Dev → new-repository → interface + impl + fake gerados da spec
5. Dev → implementa ViewModel consumindo o Repository via Koin
6. Dev → implementa Screen Composable consumindo o ViewModel
7. Testes → ViewModel testado com Fake Repository → PR
```

---

## Como Invocar as Skills

**Fluxo principal** (em negrito) — o caminho natural de uma feature:

| O que dizer | Skill | Persona |
|---|---|---|
| **`"Quero uma PRD para [task]"`** | `new-prd` | Product Manager |
| **`"Gera spec para este PRD"`** / `"Quero uma spec para [feature]"` | `new-spec` | Arquiteto |
| **`"Scaffolda a tela [nome]"`** | `new-screen` | Desenvolvedor |
| **`"Cria o composable [nome]"`** | `new-composable` | Desenvolvedor |
| **`"Gera o repository de [nome]"`** | `new-repository` | Desenvolvedor |
| **`"Revisa o código de [arquivo]"`** | `review-code` | Revisor Sênior |
| **`"QA review do [arquivo]"`** | `qa-review` | QA Engineer |

**Skills avulsas** (uso opcional, fora do fluxo principal):

| O que dizer | Skill | Quando usar |
|---|---|---|
| `"Revisa o PRD de [arquivo]"` | `review-prd` | Checar PRD existente isoladamente |
| `"Revisa a spec de [arquivo]"` | `review-spec` | Checar spec existente isoladamente |

---

## Progressive Disclosure na Implementação

`new-spec` gera tudo de uma vez (nível único): spec + checklist + plano técnico + **task breakdown com indicação de paralelismo**. O progressive disclosure aplica-se apenas às skills de **implementação**:

```
new-screen / new-composable / new-repository

L1  (padrão)         → estrutura de arquivos + assinaturas de tipos
L2  "implementa"     → código completo seguindo a spec aprovada
```

O checklist embutido na spec mostra `PRONTA PARA APROVAÇÃO` ou `PRECISA DE REVISÃO`. Você só avança para implementação depois de aprovar a spec.

---

## Exemplo Real: Do Board ao Código

**Task:** `[APP-10] Tela de resultado do scan`

| # | O que dizer | Skill | O que receber |
|---|---|---|---|
| 1 | `"Quero uma PRD para [APP-10]: tela de resultado do scan"` | `new-prd` | PRD completa em `prds/scan-result.prd.md` |
| 2 | Dev abre `prds/scan-result.prd.md` localmente, ajusta → edita arquivo OU diz "aprovado" | — | PRD aprovada |
| 3 | `"Gera spec para este PRD"` | `new-spec` | Spec completa + checklist + plano técnico + task breakdown em `specs/scan-result.spec.md` |
| 4 | Dev revisa checklist → edita arquivo OU diz "aprovado" | — | Spec aprovada + `tasks/scan-result.tasks.md` criado com o task breakdown |
| 5 | `"Scaffolda a tela ScanResult"` | `new-screen` | L1: estrutura de arquivos + assinaturas |
| 6 | `"implementa"` | `new-screen` | L2: ScanResultScreen + ScanResultViewModel (SOLID, sem Any, pt-BR) |
| 7 | `"revisa o código de ScanResultScreen.kt"` | `review-code` | 🔴 bloqueadores, 🟡 melhorias |
| 8 | Dev corrige → `"QA review do ScanResultScreen"` | `qa-review` | ✅ cobertos, ❌ gaps de cobertura |
| 9 | Dev adiciona testes, abre PR | — | Spec → `[x] Implemented` |

**Linha do tempo:**
```
APP-10 → PRD → ✅ → Spec(completa) → ✅ → tasks/ criado → Impl(L1→L2) → Review → QA → PR
```

---

## Regras que a IA Nunca Quebra

1. Não gera código de implementação sem spec aprovada.
2. Não marca Approved de forma autônoma — só quando o dev declara explicitamente na conversa.
3. Não adiciona features além do que está na spec.
4. Não usa `koinViewModel()` fora de Screen Composables.
5. Não instancia dependências diretamente — sempre via Koin/injeção.
6. Não avança de nível sem ser solicitado.
7. Não usa `Any` sem tipagem explícita.
8. Não avança da PRD para spec sem aprovação explícita do dev.

---

## Estrutura de Arquivos por Função

```
prds/               ← PRDs aprovadas das tasks do board
specs/              ← fonte da verdade para features (SDD)
tasks/              ← task breakdown de cada spec aprovada (rastreamento durante implementação)
ai/
  skills/           ← capacidades atômicas do agente
  docs/
    flow.md         ← este arquivo
app/src/main/
  feature/[name]/
    ui/             ← Screen Composables + Composables reutilizáveis
    viewmodel/      ← ViewModels (estado + eventos)
  domain/
    model/          ← entidades de domínio
    repository/     ← interfaces de Repository
    usecase/        ← Use Cases (opcional)
  data/
    repository/     ← implementações de Repository
    source/         ← DataSources (local/remote)
    fake/           ← Fakes para testes
```
