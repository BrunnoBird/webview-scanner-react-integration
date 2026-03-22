# Skill: new-screen

## Persona
Desenvolvedor

## Trigger
Invocar quando houver uma spec de **tela** com status `[x] Approved` e o desenvolvedor quiser scaffoldar a Screen Composable.

Exemplos:
- "Scaffolda a tela de resultado conforme a spec"
- "Cria a tela ScanResult"

## Pré-requisito Obrigatório
A spec da tela deve estar em `specs/` com `[x] Approved`. Se não estiver aprovada, **não executar esta skill** — invocar `new-spec` primeiro.

## Input Necessário
- Caminho da spec aprovada (ex: `specs/scan-result.spec.md`)
- Nome da tela (ex: `ScanResult` → `ScanResultScreen.kt` + `ScanResultViewModel.kt`)

## Output

### Nível 1 (padrão)
Apenas a **estrutura de arquivos** a criar, sem código:

```
Arquivos a criar:
  feature/scan/ui/ScanResultScreen.kt          ← Screen Composable (UI + eventos)
  feature/scan/viewmodel/ScanResultViewModel.kt ← ViewModel (UiState + eventos)
  feature/scan/ui/ScanResultScreen.test.kt      ← Compose UI tests

Dependências de tipos já existentes:
  domain/model/ScanResult.kt                   ← entidade de domínio (se aplicável)

Próximo passo: peça "nível 2" para ver o código completo.
```

### Nível 2 (on request: "gera o código")
Código completo da Screen Composable + ViewModel seguindo:
- Screen recebe apenas `uiState: [Name]UiState` e callbacks como lambdas — sem lógica interna
- ViewModel expõe `StateFlow<[Name]UiState>` imutável via `_uiState` privado
- `Scaffold` com `TopAppBar` e estrutura de conteúdo
- Estados de loading, error e sucesso representados por sealed class `UiState`
- Todos os textos em pt-BR
- Material 3 components

### Nível 3 (on request: "gera os composables filhos")
Composables reutilizáveis referenciados no Nível 2, extraídos para funções separadas ou arquivo próprio.

## Princípios do Desenvolvedor

O código gerado aplica SOLID no contexto Android/Compose:
- **S:** Screen orquestra layout e delega ao ViewModel — sem lógica de negócio.
- **O:** Comportamentos variáveis (callbacks, dados dinâmicos) injetados via parâmetros, não hardcoded.
- **I:** Parâmetros da Screen granulares por responsabilidade — sem objeto genérico de configuração.
- **D:** Screen recebe `viewModel: [Name]ViewModel = hiltViewModel()` — não instancia dependências.

Adicionalmente:
- Nomes de variáveis/funções/arquivos em inglês; textos visíveis ao usuário em pt-BR.
- Sem `Any` — toda tipagem derivada dos contratos da spec.
- Sem comentários óbvios — código autoexplicativo.

## Constraints
- **Nunca** usar `hiltViewModel()` dentro de Composables reutilizáveis — apenas em Screen Composables.
- **Nunca** acessar Repository diretamente na Screen — sempre via ViewModel.
- **Nunca** expor `MutableStateFlow` publicamente no ViewModel.
- **Nunca** adicionar feature não descrita na spec.
- Toda nova tela deve ter entrada no `specs/` antes de ser criada.

## Android / Compose Patterns

```kotlin
// Estrutura padrão de UiState
sealed interface ScanResultUiState {
    data object Loading : ScanResultUiState
    data class Success(val result: ScanResult) : ScanResultUiState
    data class Error(val message: String) : ScanResultUiState
}

// Estrutura padrão de ViewModel
@HiltViewModel
class ScanResultViewModel @Inject constructor(
    private val repository: ScanRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<ScanResultUiState>(ScanResultUiState.Loading)
    val uiState: StateFlow<ScanResultUiState> = _uiState.asStateFlow()
}

// Estrutura padrão de Screen
@Composable
fun ScanResultScreen(
    viewModel: ScanResultViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScanResultContent(uiState = uiState, onNavigateBack = onNavigateBack)
}
```
