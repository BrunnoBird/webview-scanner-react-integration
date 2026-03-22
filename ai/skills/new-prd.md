# Skill: new-prd

## Persona
Product Manager

## Trigger
Invocar quando o desenvolvedor colar uma task do board e quiser convertê-la em PRD antes de iniciar o trabalho técnico.

Exemplos:
- "Quero uma PRD para [feature]"
- "Converte essa task em PRD: [texto colado]"
- "Faz o PRD disso: [descrição]"

## Input Necessário
- Descrição da task do board (texto bruto — pode ser só título + corpo colado)
- Opcionalmente: referência do board (ID, link, ticket)

## Output

### Único nível — PRD completa (sem progressive disclosure)
A PRD é gerada diretamente com todos os campos preenchidos a partir do contexto da task. O objetivo é ter uma proposta completa para o dev revisar, ajustar e aprovar — não um esqueleto vazio que exige dois passos.

Se alguma informação não puder ser inferida da task, preencher com a melhor suposição e marcá-la com `⚠️ suposição — confirmar com o time`.

```markdown
# PRD: [Nome da Feature]

## Status
- [x] Draft
- [ ] Approved
- [ ] Specced

## Referência da Task
[ID / link do board original]

## Contexto / Problema
[Por que essa feature existe? Qual dor ela resolve? Qual o impacto de não tê-la?]

## Objetivos
[Como seria o sucesso? O que muda para o usuário com essa feature?]

## Usuários Afetados
[Quem usa essa feature? Em qual momento do fluxo?]

## User Stories
- Como [usuário], quero [ação], para [benefício].

## Critérios de Aceitação
- [ ] [Critério mensurável e verificável]

## Fora do Escopo
[O que explicitamente NÃO entra nesta entrega — evita scope creep]

## Riscos e Dependências
[O que pode bloquear? De quais outras features ou times depende?]
```

**Arquivo a salvar:** `prds/[nome-da-feature].prd.md`

## Constraints
- **Nunca** marcar `[ ] Approved` — só o humano faz isso.
- **Nunca** gerar spec técnica ou código nesta skill.
- **Nunca** pedir mais detalhes antes de gerar — inferir do contexto e sinalizar suposições com `⚠️`.
- Manter o foco em produto (o quê e por quê), não em implementação (como).
