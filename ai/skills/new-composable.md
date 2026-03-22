# Skill: new-composable

## Persona
Desenvolvedor

## Trigger
Invocar quando houver uma spec de **componente de UI** com status `[x] Approved` e o desenvolvedor quiser gerar o Composable reutilizável.

Exemplos:
- "Cria o composable ResultCard conforme a spec"
- "Gera o BarcodeDisplay seguindo os tipos da spec"

## Pré-requisito Obrigatório
Spec aprovada em `specs/` ou como parte de uma spec de tela. Se não houver spec aprovada, **não executar esta skill**.

## Input Necessário
- Nome do composable
- Spec de referência (caminho ou conteúdo)
- Localização desejada (ex: `feature/scan/ui/`)

## Output

### Nível 1 (padrão)
Apenas os **tipos e assinatura** do composable, sem implementação:

```kotlin
// feature/scan/ui/ResultCard.kt

@Composable
fun ResultCard(
    result: ScanResult,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // implementação no nível 2
}
```

### Nível 2 (on request: "implementa o composable")
Composable completo com:
- `remember` e `rememberUpdatedState` para estado local quando necessário
- `LaunchedEffect` para efeitos colaterais (sem acesso a dados externos)
- Feedback visual de loading, error, success via parâmetros
- Todos os textos em pt-BR
- Material 3 components, mobile-first
- `contentDescription` e `semantics` para acessibilidade

### Nível 3 (on request: "adiciona os edge cases")
- Skeleton de loading com `Placeholder` / shimmer
- Tratamento explícito de cada error case da spec
- Animações ou transições (apenas se na spec)

## Princípios do Desenvolvedor

O código gerado aplica SOLID no contexto Jetpack Compose:
- **S:** Um Composable, uma responsabilidade. Lógica de negócio ≠ lógica de UI ≠ acesso a dados. Extrair funções Composable menores quando a lógica crescer.
- **O:** Comportamentos variáveis injetados via parâmetros (callbacks, lambdas) — sem condicionais hardcoded para casos específicos.
- **I:** Parâmetros granulares e específicos — evitar `config: Map<String, Any>` ou parâmetros que nunca são usados juntos.
- **D:** Composable recebe `onAction`, `onError`, `onRetry` via parâmetros — não instancia ViewModel ou Repository diretamente.

Adicionalmente:
- Nomes de variáveis/funções/arquivos em inglês; textos visíveis ao usuário em pt-BR.
- Sem `Any` — toda tipagem derivada dos contratos da spec.
- Sem comentários óbvios — código autoexplicativo.

## Constraints
- **Nunca** usar `hiltViewModel()` em Composables reutilizáveis — apenas em Screen Composables.
- **Nunca** acessar Repository, UseCase ou DataSource diretamente no Composable.
- **Nunca** adicionar feature não descrita na spec (ex: não adicionar campo de edição se a spec não pede).
- **Nunca** criar abstração para uso único.

## Compose Patterns

```kotlin
// Padrão de parâmetro de estado + evento
@Composable
fun ResultCard(
    result: ScanResult,         // estado (dados)
    onDismiss: () -> Unit,      // evento (D: injetado pelo pai)
    modifier: Modifier = Modifier,
) { ... }

// Padrão de LaunchedEffect com chave estável
LaunchedEffect(result.id) {
    // efeito que depende do resultado — re-executa só quando id muda
}

// Padrão de acessibilidade
IconButton(
    onClick = onDismiss,
    modifier = Modifier.semantics { contentDescription = "Fechar resultado" }
) {
    Icon(Icons.Default.Close, contentDescription = null)
}
```
