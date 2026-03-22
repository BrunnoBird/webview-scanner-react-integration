# Spec: WebView Scanner Screen — Android

## Status
- [x] Draft
- [ ] Approved
- [ ] Implemented
- [ ] Tested

## Purpose
Prover uma tela Android com WebView que carrega uma URL de localhost configurável e
que, ao receber uma chamada do WebApp, abre o ML Kit Code Scanner nativo, lê códigos de
barras (boleto CODE_128) ou QR Codes (Pix) e empurra o resultado de volta à WebView via
`window.VibeBird.onScanResult(payload)`.

> Derivado de: `prds/ml-kit-scan-bridge.prd.md`
> Contrato de payload: `specs/ml-kit-scan-bridge.spec.md`

---

## Contracts

### Types (Kotlin)

```kotlin
// MVI — Intent (eventos da UI / WebApp)
sealed class WebViewScannerIntent {
    data object LoadUrl : WebViewScannerIntent()
    data object StartScan : WebViewScannerIntent()
}

// MVI — Effect (efeitos colaterais únicos)
sealed class WebViewScannerEffect {
    data object OpenMlKitScanner : WebViewScannerEffect()
    data class PushScanResultToWebView(val jsonPayload: String) : WebViewScannerEffect()
    data class ShowError(val message: String) : WebViewScannerEffect()
}

// MVI — Estado único da tela
data class WebViewScannerState(
    val url: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

// Domínio — resultado do scan
sealed class ScanResult {
    data class Success(
        val value: String,
        val type: ScanType,
        val rawFormat: String?
    ) : ScanResult()

    data class Failure(
        val code: ScanErrorCode,
        val message: String? = null
    ) : ScanResult()
}

enum class ScanType { BOLETO, PIX, UNKNOWN }

enum class ScanErrorCode {
    CAMERA_DENIED,
    NO_BARCODE_FOUND,
    TIMEOUT,
    CANCELLED,
    UNKNOWN_ERROR
}
```

### Inputs
- URL de localhost fornecida via configuração (BuildConfig ou similar).
- Chamada JavaScript do WebApp acionando o scanner (via `JavascriptInterface` ou deep link interno).
- Resultado do ML Kit (`Barcode`, cancelamento ou exceção).

### Outputs / Side Effects
- WebView renderiza a URL configurada.
- ML Kit Code Scanner é aberto em uma nova Activity gerenciada pelo SDK GMS.
- Após o scan, `webView.evaluateJavascript("window.VibeBird && window.VibeBird.onScanResult($payload)", null)` é chamado na UI thread.
- Permissão de câmera é solicitada em runtime antes de iniciar o scan.

---

## Behavior

### Happy Path

1. **App inicializa** → `MainActivity` navega para `WebViewScannerScreen`.
2. **Screen monta** → dispara `WebViewScannerIntent.LoadUrl` → ViewModel lê a URL de localhost via `BuildConfig.WEBVIEW_BASE_URL`.
3. **Estado atualizado** → `WebViewScannerState(url = "http://...", isLoading = true)`.
4. **WebView configurada** com `settings.javaScriptEnabled = true` e `WebViewClient` que monitora `onPageFinished` → ao terminar, `isLoading = false`.
5. **`AndroidScanBridge` registrado** via `webView.addJavascriptInterface(bridge, "AndroidBridge")` antes do `loadUrl` — expõe o método `startScan()` acessível pelo JS como `window.AndroidBridge.startScan()`.
6. **WebApp aciona o scanner** → JS chama `window.AndroidBridge.startScan()` → `AndroidScanBridge.startScan()` dispara `WebViewScannerIntent.StartScan` no ViewModel via callback.
7. **ViewModel emite** `WebViewScannerEffect.OpenMlKitScanner`.
8. **Screen observa o effect** → verifica permissão de câmera:
   - Permissão concedida → inicia `GmsBarcodeScanning.getClient(options).startScan()`.
   - Permissão pendente → solicita via `ActivityResultContracts.RequestPermission`.
9. **ML Kit abre** a Activity de scan nativa (Google Code Scanner UI).
10. **Usuário escaneia** boleto (CODE_128) ou QR Pix (QR_CODE).
11. **`addOnSuccessListener`** recebe `Barcode` → ViewModel constrói `ScanResult.Success(value, type, rawFormat)` → serializa como JSON conforme contrato (`specs/ml-kit-scan-bridge.spec.md`).
12. **ViewModel emite** `WebViewScannerEffect.PushScanResultToWebView(jsonPayload)`.
13. **Screen executa na UI thread:**
    ```
    webView.evaluateJavascript(
        "window.VibeBird && window.VibeBird.onScanResult($jsonPayload)",
        null
    )
    ```
14. **WebApp recebe** o evento em `window.VibeBird.onScanResult` e processa o resultado.

---

### Error / Edge Cases

| Cenário | Origem | Comportamento |
|---|---|---|
| **Câmera negada pelo usuário** | `RequestPermission` retorna `false` | ViewModel constrói `ScanResult.Failure(CAMERA_DENIED, "Permissão de câmera negada pelo usuário")` → emite `PushScanResultToWebView` com payload de erro |
| **Usuário fecha o scanner (voltar)** | `addOnCanceledListener` disparado | `ScanResult.Failure(CANCELLED)` → push para WebView sem `message` |
| **Timeout / nenhum código lido** | `addOnFailureListener` com mensagem contendo "timeout" | `ScanResult.Failure(NO_BARCODE_FOUND, mensagem)` → push para WebView |
| **Exceção genérica do ML Kit** | `addOnFailureListener` com outra exceção | `mapExceptionToCode(e)` → `TIMEOUT` ou `UNKNOWN_ERROR` → push para WebView com `message` |
| **`window.VibeBird` não registrado** | WebApp ainda carregando quando push ocorre | Guard `window.VibeBird &&` na string JS → no-op silencioso; nenhum crash |
| **URL de localhost inacessível** | `onReceivedError` no `WebViewClient` | `WebViewScannerState(error = "Não foi possível carregar a página")` → Screen exibe mensagem de erro na UI; funcionalidade de scan não é afetada |
| **Google Play Services indisponível** | Exceção ao instanciar `GmsBarcodeScanning` | Capturado no bloco `try/catch` → `ScanResult.Failure(UNKNOWN_ERROR, e.message)` → push para WebView |
| **`startScan()` chamado antes da permissão ser resolvida** | Chamada JS dupla enquanto diálogo de permissão está aberto | ViewModel ignora intents `StartScan` enquanto `isScanning = true` no estado; flag de guarda no ViewModel |

---

## Integration Points

| Módulo | Papel |
|---|---|
| `feature/webviewscanner/ui/WebViewScannerScreen.kt` | Screen Composable — renderiza WebView, observa effects, gerencia ciclo de vida da WebView |
| `feature/webviewscanner/viewmodel/WebViewScannerViewModel.kt` | ViewModel MVI — processa intents, emite effects, mantém estado único |
| `feature/webviewscanner/bridge/AndroidScanBridge.kt` | `@JavascriptInterface` exposto como `window.AndroidBridge` — recebe `startScan()` do JS e delega ao ViewModel |
| `specs/ml-kit-scan-bridge.spec.md` | Contrato de payload JSON (estrutura exata do JSON enviado via `evaluateJavascript`) |
| `AndroidManifest.xml` | Permissões `CAMERA` e `INTERNET`; `network_security_config` para HTTP em localhost |
| `res/xml/network_security_config.xml` | Libera tráfego HTTP para `localhost` e `10.0.2.2` (emulador) |
| ML Kit GMS `GmsBarcodeScanning` | SDK externo — `GmsBarcodeScannerOptions` com `FORMAT_CODE_128` e `FORMAT_QR_CODE` |
| Koin DI | `WebViewScannerViewModel` provido via `koinViewModel()` exclusivamente na Screen |

---

## Out of Scope

- Implementação do lado web/JS — hook `useScanEvent`, `window.VibeBird`, mock — cobertos pela `specs/ml-kit-scan-bridge.spec.md`.
- Leitura de outros formatos de código de barras além de `CODE_128` e `QR_CODE`.
- Autenticação, HTTPS ou certificados para a URL de localhost.
- Navegação entre múltiplas URLs dentro da WebView (sem suporte a `shouldOverrideUrlLoading` avançado).
- Retry automático de scan sem nova chamada do WebApp.
- Histórico ou persistência de resultados de scan.
- Tratamento de múltiplos códigos no mesmo frame (ML Kit retorna apenas o primeiro reconhecido).
- Suporte a dispositivos sem Google Play Services (apenas GMS; variante ML Kit local fora do escopo).

---

## Open Questions

1. **Mecanismo de acionamento do scanner:** O WebApp deve chamar `window.AndroidBridge.startScan()` via `addJavascriptInterface`. Confirmar com o time web se o nome `AndroidBridge` é o combinado ou se há preferência diferente.

2. **URL de localhost — forma de configuração:** `BuildConfig.WEBVIEW_BASE_URL` (definida em `build.gradle`) é a abordagem recomendada para dev/QA/prod. Confirmar se há necessidade de configuração em runtime (ex: campo de input de URL para debug).

3. **`network_security_config` escopo:** A liberação de HTTP para `localhost` e `10.0.2.2` deve ser restrita apenas ao `debug` build type? Confirmar com o time de segurança antes de incluir no `release`.

4. **Google Play Services obrigatório:** O app já tem GMS como requisito? Se o target inclui devices sem GMS (ex: Huawei), será necessário avaliar o ML Kit bundled (`com.google.mlkit:barcode-scanning`) como alternativa.

---

## ✅ Checklist de Completude

✅ Purpose em uma frase clara
✅ Contracts com sealed classes / data classes / enums Kotlin
✅ Inputs definidos (URL, chamada JS, resultado ML Kit)
✅ Outputs / Side Effects definidos (WebView, scanner, evaluateJavascript, permissão)
✅ Happy Path descrito passo a passo (14 etapas)
✅ Error / Edge Cases listados (8 cenários com origem e comportamento)
✅ Integration Points mapeados (8 módulos com papéis)
✅ Out of Scope explícito (8 itens)
✅ Open Questions documentadas (4 questões com contexto)

**Resultado: PRONTA PARA APROVAÇÃO**
