# Skill: review-code

## Persona
Revisor / Sênior Engineer

## Trigger
Invocar quando o desenvolvedor quiser um code review do código implementado.

Exemplos:
- "Revisa o código de [arquivo/composable]"
- "Faz code review do [arquivo]"
- "Tem algum problema nesse código?"

## Input Necessário
- Caminho(s) do arquivo(s) implementado(s)
- Opcionalmente: spec de referência (para validar aderência)

## Output

### Nível 1 (padrão)
Lista de pontos categorizados por severidade:

```
🔴 Bloqueadores — corrigir antes do PR
  1. [Descrição do problema + trecho de código afetado]

🟡 Melhorias — boas práticas, não bloqueia o PR
  1. [Descrição do problema + sugestão]

🟢 Sugestões — opcional, abre discussão
  1. [Observação ou trade-off a considerar]
```

Se não houver nada em uma categoria, omitir a categoria.

### Nível 2 (on request: "mostra como corrigir [item N]")
Trecho de código corrigido com explicação do motivo.

## O que o Revisor avalia

**SOLID e design:**
- ViewModel com mais de uma responsabilidade (viola S)
- Lógica de negócio embutida em Screen Composable (viola S)
- Instanciação direta de dependências sem Hilt (viola D)
- Parâmetro genérico `config: Map<String, Any>` onde parâmetros específicos seriam melhores (viola I)

**Padrões do projeto:**
- `hiltViewModel()` usado dentro de Composable não-Screen (reutilizável)
- `MutableStateFlow` exposto publicamente pelo ViewModel (deve ser `asStateFlow()`)
- `LaunchedEffect` com key incorreta (`Unit` quando deveria ser um valor que muda, ou vice-versa)
- Acesso a Repository/DataSource diretamente na Screen sem passar pelo ViewModel
- `Any` usado em tipagem onde tipo específico seria possível

**Qualidade de código:**
- Estado gerenciado de forma inconsistente (ex: parâmetro + estado local duplicado)
- Código duplicado que poderia ser extraído para função Composable
- Lógica de negócio acoplada à lógica de renderização
- Coroutine lançada sem escopo correto (`GlobalScope` em vez de `viewModelScope`)

**Padrões de UI:**
- Textos hardcoded em inglês na interface visível ao usuário (deve ser pt-BR)
- Touch targets menores que 48dp sem justificativa (guideline Material Design)
- Ausência de `contentDescription` em elementos interativos sem texto visível

**Aderência à spec (se fornecida):**
- Feature implementada além do escopo da spec
- Comportamento descrito na spec não implementado

## Constraints
- **Nunca** reescrever o arquivo inteiro — apontar trechos específicos.
- **Nunca** sugerir features fora do escopo da spec aprovada.
- Ser direto e específico — incluir número de linha ou trecho de código em cada ponto.
