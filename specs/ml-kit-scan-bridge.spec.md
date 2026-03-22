# Spec: ML Kit Scan Bridge — Recepção de Eventos Nativo → WebView

## Status
- [ ] Draft
- [X] Approved
- [X] Implemented
- [X] Tested

## Purpose
Preparar a WebView para receber, de forma tipada e robusta, os eventos de **sucesso** e **erro** emitidos pelo scanner de código de barras ML Kit nativo do Android, via padrão event-push (`window.VibeBird.onScanResult`).

> Derivado de: `prds/ml-kit-scan-bridge.prd.md`

---

## Contracts

### Types (`lib/native-bridge/types.ts` — adições)

```typescript
/** Código de erro padronizado enviado pelo Android após falha no scan. */
export type ScanErrorCode =
  | "CAMERA_DENIED"
  | "NO_BARCODE_FOUND"
  | "TIMEOUT"
  | "CANCELLED"
  | "UNKNOWN_ERROR";

/** Payload enviado pelo Android em caso de scan bem-sucedido. */
export interface ScanSuccessPayload {
  status: "success";
  /** Linha digitável do boleto ou chave Pix decodificada. */
  value: string;
  /** Tipo inferido pelo nativo ou pelo web. */
  type: ScanResultType; // reutiliza tipo já existente: "boleto" | "pix" | "unknown"
  /** Formato raw retornado pelo ML Kit (ex: "CODE_128", "QR_CODE"). */
  rawFormat?: string;
}

/** Payload enviado pelo Android em caso de falha ou cancelamento. */
export interface ScanErrorPayload {
  status: "error";
  code: ScanErrorCode;
  /** Mensagem humana opcional para log/debug. */
  message?: string;
}

/** Union discriminada — único tipo que atravessa o bridge. */
export type ScanEventPayload = ScanSuccessPayload | ScanErrorPayload;

/** Namespace global registrado pela WebView para receber eventos do nativo. */
export interface VibeBirdGlobal {
  onScanResult(payload: ScanEventPayload): void;
}

declare global {
  interface Window {
    VibeBird?: VibeBirdGlobal;
  }
}
```

### Inputs
- Android ML Kit conclui leitura de código de barras (sucesso ou falha).
- Android chama `window.VibeBird.onScanResult(payload)` via `evaluateJavascript()`.

### Outputs / Side Effects
- Hook `useScanEvent` emite novo estado: `{ result, error, isScanning }`.
- Componentes consumidores re-renderizam com o valor lido ou com mensagem de erro.
- `window.VibeBird` é registrado globalmente pela WebView antes do React montar.

---

## Behavior

### Happy Path
_(Deixar para o dev preencher ou solicitar L2)_

### Error / Edge Cases
_(Deixar para o dev preencher ou solicitar L2)_

---

## Integration Points

| Módulo | Papel |
|---|---|
| `lib/native-bridge/types.ts` | Recebe os novos tipos (`ScanEventPayload`, `ScanSuccessPayload`, `ScanErrorPayload`, `ScanErrorCode`, `VibeBirdGlobal`) |
| `lib/native-bridge/mock.ts` | Simula push de evento após delay (sem Promise — push direto via callback) |
| `lib/native-bridge/index.ts` | Expõe `registerScanListener(cb): () => void` |
| `hooks/useScanEvent.ts` | Hook React com estado `{ result, error, isScanning }` |
| `app/_document.tsx` ou script global | Registra `window.VibeBird` antes do React hidratar |
| `specs/scanner-bridge.spec.md` | Spec existente (Pull/Promise) — complementada, não substituída |

---

## Out of Scope
_(Deixar para o dev preencher ou solicitar L2)_

---

## Open Questions

1. **Mecanismo de push:** O time Android vai usar `evaluateJavascript` (recomendado) ou `addJavascriptInterface`? A escolha determina como `window.VibeBird` precisa ser registrado.
2. **Coexistência com `openScanner()`:** Este spec introduz padrão Push. Os dois padrões coexistem ou este substitui o Pull da `scanner-bridge.spec.md`?
3. **Timing de registro:** `window.VibeBird` deve ser registrado antes do Android chamar `onScanResult`. Onde registrar? (`_app.tsx`, script inline no `<head>`, ou `layout.tsx`)?

---

## Contrato Android — Guia de Implementação Nativa

> Esta seção é destinada ao **time Android**. Define exatamente o que o lado Kotlin deve fazer após o ML Kit retornar um resultado, para que a WebView receba e processe corretamente.

### Mecanismo: `evaluateJavascript`

O Android **não** usa `addJavascriptInterface` para este fluxo de push. Após o ML Kit concluir o scan, o Android serializa o resultado como JSON e chama:

```kotlin
webView.evaluateJavascript(
    "window.VibeBird && window.VibeBird.onScanResult($jsonPayload)",
    null
)
```

O guard `window.VibeBird &&` garante que a chamada é no-op caso a WebView ainda não tenha registrado o handler (evita `TypeError` no JS).

---

### Formato do Payload JSON

#### Sucesso — Boleto

```json
{
  "status": "success",
  "value": "23793380296040641000260006401011291230000010000",
  "type": "boleto",
  "rawFormat": "CODE_128"
}
```

#### Sucesso — Pix (QR Code)

```json
{
  "status": "success",
  "value": "00020126330014br.gov.bcb.pix01114455667789011520400005303986540510.005802BR",
  "type": "pix",
  "rawFormat": "QR_CODE"
}
```

#### Erro — Câmera negada

```json
{
  "status": "error",
  "code": "CAMERA_DENIED",
  "message": "Permissão de câmera negada pelo usuário"
}
```

#### Erro — Nenhum código encontrado

```json
{
  "status": "error",
  "code": "NO_BARCODE_FOUND",
  "message": "Nenhum código de barras detectado após timeout"
}
```

#### Cancelamento pelo usuário

```json
{
  "status": "error",
  "code": "CANCELLED"
}
```

---

### Códigos de Erro — Tabela de Mapeamento

| `code` | Quando usar |
|---|---|
| `CAMERA_DENIED` | `ActivityResultContracts.RequestPermission` retornou `false` |
| `NO_BARCODE_FOUND` | Timeout do scanner sem nenhum código reconhecido |
| `TIMEOUT` | Timeout geral da Activity de scan (ex: usuário demorou demais) |
| `CANCELLED` | Usuário pressionou voltar / fechou a Activity de scan |
| `UNKNOWN_ERROR` | Exceção não mapeada no `catch` |

---

### Exemplo Kotlin — Integração Completa

```kotlin
// ScannerActivity.kt (ou Fragment equivalente)

import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import org.json.JSONObject

class ScannerActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    fun startScan() {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_CODE_128,    // boleto
                Barcode.FORMAT_QR_CODE      // Pix
            )
            .build()

        val scanner = GmsBarcodeScanning.getClient(this, options)

        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val payload = buildSuccessPayload(barcode)
                pushToWebView(payload)
            }
            .addOnCanceledListener {
                val payload = buildErrorPayload("CANCELLED", null)
                pushToWebView(payload)
            }
            .addOnFailureListener { exception ->
                val code = mapExceptionToCode(exception)
                val payload = buildErrorPayload(code, exception.message)
                pushToWebView(payload)
            }
    }

    // -------------------------------------------------------------------------
    // Helpers de payload
    // -------------------------------------------------------------------------

    private fun buildSuccessPayload(barcode: Barcode): String {
        val value = barcode.rawValue ?: ""
        val type = inferType(barcode)
        val rawFormat = formatName(barcode.format)

        return JSONObject().apply {
            put("status", "success")
            put("value", value)
            put("type", type)
            if (rawFormat != null) put("rawFormat", rawFormat)
        }.toString()
    }

    private fun buildErrorPayload(code: String, message: String?): String {
        return JSONObject().apply {
            put("status", "error")
            put("code", code)
            if (!message.isNullOrBlank()) put("message", message)
        }.toString()
    }

    /** Infere o tipo para a WebView baseado no formato do ML Kit. */
    private fun inferType(barcode: Barcode): String {
        return when (barcode.format) {
            Barcode.FORMAT_QR_CODE  -> "pix"
            Barcode.FORMAT_CODE_128 -> "boleto"
            else                    -> "unknown"
        }
    }

    /** Converte a constante inteira do ML Kit para string legível. */
    private fun formatName(format: Int): String? {
        return when (format) {
            Barcode.FORMAT_CODE_128 -> "CODE_128"
            Barcode.FORMAT_QR_CODE  -> "QR_CODE"
            Barcode.FORMAT_EAN_13   -> "EAN_13"
            Barcode.FORMAT_PDF417   -> "PDF417"
            else                    -> null
        }
    }

    /** Mapeia exceções do ML Kit / câmera para os códigos do contrato. */
    private fun mapExceptionToCode(e: Exception): String {
        return when {
            e.message?.contains("permission", ignoreCase = true) == true -> "CAMERA_DENIED"
            e.message?.contains("timeout", ignoreCase = true) == true    -> "TIMEOUT"
            else                                                          -> "UNKNOWN_ERROR"
        }
    }

    // -------------------------------------------------------------------------
    // Push para WebView
    // -------------------------------------------------------------------------

    /**
     * Serializa o payload e chama window.VibeBird.onScanResult() na WebView.
     * DEVE ser executado na thread principal (UI thread).
     */
    private fun pushToWebView(jsonPayload: String) {
        val js = "window.VibeBird && window.VibeBird.onScanResult($jsonPayload)"
        runOnUiThread {
            webView.evaluateJavascript(js, null)
        }
    }
}
```

---

### Checklist para o Time Android

- [ ] `GmsBarcodeScannerOptions` configurado com `FORMAT_CODE_128` e `FORMAT_QR_CODE`
- [ ] `addOnSuccessListener` chama `pushToWebView` com payload `status: "success"`
- [ ] `addOnCanceledListener` chama `pushToWebView` com `code: "CANCELLED"`
- [ ] `addOnFailureListener` chama `pushToWebView` com o código mapeado
- [ ] `pushToWebView` executa na UI thread (`runOnUiThread`)
- [ ] Guard `window.VibeBird &&` presente na string JS (evita crash se WebView não carregou)
- [ ] `rawFormat` usa as strings do contrato (`"CODE_128"`, `"QR_CODE"` etc.), não as constantes inteiras
- [ ] Testado com WebView em modo `DEBUG` (`WebView.setWebContentsDebuggingEnabled(true)`) para inspecionar via Chrome DevTools
