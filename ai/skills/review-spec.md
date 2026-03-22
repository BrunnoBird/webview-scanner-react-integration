# Skill: review-spec

## Trigger
Invocar quando o desenvolvedor quiser revisar ou aprovar uma spec existente.

Exemplos:
- "Revisa a spec do repositório de pagamento"
- "A spec da tela de resultado está completa?"
- "Posso aprovar esta spec?"

## Input Necessário
- Caminho da spec a revisar (ex: `specs/scan-result.spec.md`)
- Ou: conteúdo da spec colado diretamente

## Output

### Nível 1 (padrão)
Checklist de completude contra o template SDD:

```
✅ / ❌  Status preenchido
✅ / ❌  Purpose em uma frase clara
✅ / ❌  Contracts com data classes / sealed classes Kotlin
✅ / ❌  Inputs definidos
✅ / ❌  Outputs / Side Effects definidos
✅ / ❌  Happy Path descrito passo a passo
✅ / ❌  Error / Edge Cases listados
✅ / ❌  Integration Points mapeados
✅ / ❌  Out of Scope explícito
✅ / ❌  Open Questions resolvidas ou documentadas

Resultado: PRONTA PARA APROVAÇÃO / PRECISA DE REVISÃO
```

### Nível 2 (on request: "detalha os problemas")
Para cada `❌`, explicar o que está faltando e sugerir o conteúdo.

### Nível 3 (on request: "sugere melhorias")
Sugestões de open questions adicionais, edge cases não cobertos, ou ambiguidades no contrato.

## Constraints
- **Nunca** alterar o arquivo de spec diretamente — apenas reportar.
- **Nunca** marcar como `Approved` — isso é papel do desenvolvedor.
- **Nunca** sugerir código de implementação nesta skill.

## Checklist específico para specs de Repository / DataSource
Além do template padrão, verificar para features que tocam a camada de dados:
- [ ] Estratégia de Coroutine está definida (`suspend fun` vs `Flow`)?
- [ ] Comportamento de erro está definido (sealed `Result`, exceção tipada)?
- [ ] Escopo de Coroutine está endereçado (`viewModelScope`, `lifecycleScope`)?
- [ ] Interface de domínio está separada da implementação de dados?
- [ ] Existe um Fake previsto para testes?
