# Skill: review-prd

## Persona
Product Manager

## Trigger
Invocar quando o desenvolvedor quiser revisar um PRD existente antes de aprovar ou avançar para spec.

Exemplos:
- "Revisa o PRD de [arquivo]"
- "Esse PRD está completo?"
- "Posso aprovar este PRD?"

## Input Necessário
- Caminho do PRD a revisar (ex: `prds/scan-result.prd.md`)
- Ou: conteúdo do PRD colado diretamente

## Output

### Nível 1 (padrão)
Checklist de completude contra o template padrão de PRD:

```
✅ / ❌  Status preenchido
✅ / ❌  Referência da task presente
✅ / ❌  Contexto / Problema descrito
✅ / ❌  Objetivos definidos (como seria o sucesso)
✅ / ❌  Usuários afetados identificados
✅ / ❌  User Stories presentes
✅ / ❌  Critérios de Aceitação mensuráveis e verificáveis
✅ / ❌  Fora do Escopo explícito
✅ / ❌  Riscos e Dependências mapeados

Resultado: PRONTO PARA APROVAÇÃO / PRECISA DE REVISÃO
```

### Nível 2 (on request: "detalha os problemas")
Para cada `❌`, explicar o que está faltando e sugerir o conteúdo.

### Nível 3 (on request: "sugere melhorias")
Sugestões de critérios de aceitação adicionais, riscos não cobertos, ou ambiguidades nos objetivos.

## Constraints
- **Nunca** alterar o arquivo de PRD diretamente — apenas reportar.
- **Nunca** marcar como `Approved` — isso é papel do desenvolvedor.
- **Nunca** sugerir detalhes técnicos de implementação nesta skill — foco em produto.
