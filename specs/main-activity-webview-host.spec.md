# Spec: MainActivity WebView Host

## Status
- [ ] Draft
- [x] Approved
- [x] Implemented
- [ ] Implemented
- [ ] Tested

## Purpose
Substituir o placeholder Compose da `MainActivity` por uma Screen Composable que hospeda
uma WebView, carregando automaticamente a URL de localhost configurável via `BuildConfig`,
ao abrir o app.

> Derivado de: `prds/main-activity-webview-host.prd.md`

---

## Contracts

### Types (Kotlin)

```kotlin
// MVI — Intent (eventos da UI)
sealed class MainWebViewIntent {
    data object LoadUrl : MainWebViewIntent()
    data object OnBackPressed : MainWebViewIntent()
}

// MVI — Effect (efeitos colaterais únicos)
sealed class MainWebViewEffect {
    data class HandleBackNavigation(val canGoBack: Boolean) : MainWebViewEffect()
}

// MVI — Estado único da tela
data class MainWebViewState(
    val url: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)
```

### Inputs
- `BuildConfig.WEBVIEW_BASE_URL` (String) — URL de localhost definida via `buildConfigField` no `build.gradle.kts`.
- Intent `OnBackPressed` — disparado pelo `BackHandler` do Compose quando o usuário pressiona voltar.

### Outputs / Side Effects
- WebView ocupa toda a área da tela (edge-to-edge) e renderiza a WebApp.
- Histórico de navegação da WebView é respeitado na ação de voltar.
- Erro de carregamento é refletido em `MainWebViewState.error` e exibido na UI.

---

## Behavior

### Happy Path

1. **App abre** → `MainActivity.onCreate()` é chamado.
2. **`enableEdgeToEdge()`** é ativado; `setContent {}` renderiza `MainWebViewScreen`.
3. **Screen monta** → `koinViewModel<MainWebViewViewModel>()` injeta o ViewModel via Koin.
4. **Screen dispara** `MainWebViewIntent.LoadUrl` → ViewModel lê `BuildConfig.WEBVIEW_BASE_URL`.
5. **Estado atualizado:** `MainWebViewState(url = "http://10.0.2.2:<PORTA>", isLoading = true)`.
6. **WebView configurada:** `settings.javaScriptEnabled = true`; `WebViewClient` monitorando `onPageFinished`.
7. **`webView.loadUrl(url)`** chamado → página começa a carregar.
8. **`onPageFinished` dispara** → `MainWebViewState(isLoading = false)` → WebApp visível ao usuário.

### Error / Edge Cases

| Cenário | Origem | Comportamento |
|---|---|---|
| **Back press com histórico na WebView** | `BackHandler` + `webView.canGoBack() == true` | ViewModel emite `HandleBackNavigation(canGoBack = true)` → Screen chama `webView.goBack()` |
| **Back press sem histórico na WebView** | `BackHandler` + `webView.canGoBack() == false` | ViewModel emite `HandleBackNavigation(canGoBack = false)` → `BackHandler` não consome o evento → sistema fecha a Activity normalmente |
| **URL de localhost inacessível** | `WebViewClient.onReceivedError` | `MainWebViewState(error = "Não foi possível carregar a página")` → Screen exibe mensagem de erro em PT-BR |
| **`BuildConfig.WEBVIEW_BASE_URL` vazia** | Configuração ausente no `build.gradle.kts` | ⚠️ suposição — lançar `IllegalStateException` em builds debug; definir fallback ou bloquear build em release |

---

## Integration Points

| Módulo | Papel |
|---|---|
| `MainActivity.kt` | Entry point do app — `setContent {}` hospeda `MainWebViewScreen`; código placeholder (`Greeting`, `GreetingPreview`) removido |
| `feature/mainwebview/ui/MainWebViewScreen.kt` | Screen Composable — `AndroidView(::WebView)`, `BackHandler`, coleta `StateFlow` e `effects` do ViewModel |
| `feature/mainwebview/viewmodel/MainWebViewViewModel.kt` | ViewModel MVI — processa intents, emite effects via `Channel`, mantém `StateFlow<MainWebViewState>` |
| `AndroidManifest.xml` | Permissão `INTERNET` + atributo `android:networkSecurityConfig` apontando para `@xml/network_security_config` |
| `res/xml/network_security_config.xml` | Libera tráfego HTTP cleartext para `localhost` e `10.0.2.2` (emulador Android) |
| `app/build.gradle.kts` | `buildConfigField("String", "WEBVIEW_BASE_URL", "\"http://10.0.2.2:<PORTA>\"")` em `defaultConfig` |
| Koin DI | `MainWebViewViewModel` declarado em módulo Koin; `koinViewModel()` chamado exclusivamente em `MainWebViewScreen` |

---

## Out of Scope

- Scanner bridge JS (`window.AndroidBridge.startScan`) — coberto por `specs/webview-scanner-screen.spec.md`.
- Autenticação, HTTPS ou certificados TLS para a URL de localhost.
- Deep links ou navegação entre múltiplas URLs dentro da WebView.
- Tela de loading/splash customizada durante o carregamento inicial.
- Página de erro customizada (WebView Chrome error page) — apenas mensagem textual simples.
- Limpeza de cache ou histórico da WebView.

---

## Open Questions

1. **Porta do localhost:** qual porta a WebApp usa em desenvolvimento? (`3000`, `8080`, outro?) — necessário para definir `WEBVIEW_BASE_URL` corretamente no `build.gradle.kts`.

2. **`network_security_config` restrito a debug?** A liberação de HTTP cleartext deve ser limitada ao `debug` build type? Confirmar com o time de segurança antes de incluir no `release`.

3. **Relação com `specs/webview-scanner-screen.spec.md`:** essa spec está em Draft e define uma tela WebView com scanner bridge completo. As duas specs convergem para a mesma tela evoluída, ou a `MainWebViewScreen` desta spec é a base sobre a qual a outra será construída?

---

## ✅ Checklist de Completude

✅ Purpose em uma frase clara
✅ Contracts com sealed classes / data class Kotlin
✅ Inputs definidos (BuildConfig URL, BackPressed intent)
✅ Outputs / Side Effects definidos (WebView fullscreen, back navigation, error state)
✅ Happy Path descrito passo a passo (8 etapas)
✅ Error / Edge Cases listados (4 cenários com origem e comportamento)
✅ Integration Points mapeados (7 módulos com papéis)
✅ Out of Scope explícito (6 itens)
✅ Open Questions documentadas (3 questões com contexto)

**Resultado: PRONTA PARA APROVAÇÃO**

---

## 📐 Plano Técnico de Implementação

> Sem código — apenas o plano arquitetural para orientar a implementação.

### Arquivos a criar
```
feature/mainwebview/ui/MainWebViewScreen.kt       ← Screen Composable (AndroidView + BackHandler)
feature/mainwebview/viewmodel/MainWebViewViewModel.kt  ← ViewModel MVI (StateFlow + Channel de effects)
res/xml/network_security_config.xml               ← cleartext traffic para localhost / 10.0.2.2
```

### Arquivos a modificar
```
MainActivity.kt                 ← remover Greeting/GreetingPreview; chamar MainWebViewScreen
AndroidManifest.xml             ← adicionar INTERNET permission + networkSecurityConfig
app/build.gradle.kts            ← adicionar buildConfigField WEBVIEW_BASE_URL
```

### Padrões SOLID aplicados
- **S** — `MainWebViewScreen` lida exclusivamente com UI e eventos; `MainWebViewViewModel` lida exclusivamente com estado e lógica MVI.
- **O** — URL e comportamentos de erro injetados via `BuildConfig` e `State`, não hardcoded na Screen.
- **D** — ViewModel recebe dependências via Koin — nunca instanciadas diretamente; Screen depende da abstração `StateFlow`, não da implementação do ViewModel.

### Ordem de implementação recomendada
1. Tipos Kotlin: `MainWebViewIntent`, `MainWebViewEffect`, `MainWebViewState` (sem dependências)
2. `res/xml/network_security_config.xml` e `app/build.gradle.kts` (sem dependências entre si)
3. `AndroidManifest.xml` (depende do `network_security_config.xml` existir)
4. `MainWebViewViewModel.kt` (depende dos tipos do passo 1 e da URL do passo 2)
5. `MainWebViewScreen.kt` (depende do ViewModel)
6. `MainActivity.kt` (depende da Screen)

### Próximo passo
Diga "aprovado" para o agente atualizar o status do arquivo.
Em seguida, invocar: `new-screen` para `MainWebViewScreen` e `new-composable` para `MainWebViewViewModel`.

---

## 🗂️ Task Breakdown

> Tarefas derivadas do plano técnico. Grupos paralelos podem ser iniciados simultaneamente.

**Grupo A — ⚡ Parallelizável (sem dependências entre si):**
- [ ] T1: Criar `MainWebViewIntent`, `MainWebViewEffect`, `MainWebViewState` — manual (tipos Kotlin)
- [ ] T2: Criar `res/xml/network_security_config.xml` — manual
- [ ] T3: Adicionar `buildConfigField("WEBVIEW_BASE_URL", ...)` em `app/build.gradle.kts` — manual

**Grupo B — Após Grupo A:**
- [ ] T4: Atualizar `AndroidManifest.xml` (INTERNET + `networkSecurityConfig`) — depende de: T2 — manual
- [ ] T5: Criar `MainWebViewViewModel.kt` — depende de: T1, T3 — skill: `new-composable`

**Grupo C — Sequential (após Grupo B completo):**
- [ ] T6: Criar `MainWebViewScreen.kt` — depende de: T5 — skill: `new-screen`
- [ ] T7: Atualizar `MainActivity.kt` (remover placeholder, chamar `MainWebViewScreen`) — depende de: T6 — manual

**Dependências resumidas:**
```
T1 ──┐
T3 ──┤── T5 ──┐
T2 ── T4      ├── T6 ── T7
```
