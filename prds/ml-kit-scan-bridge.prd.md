# PRD: WebView Scanner Bridge — Android

## Status
- [ ] Draft
- [x] Approved
- [ ] Specced

## Referência da Task
> ⚠️ suposição — confirmar com o time: sem ID de board fornecido. Vincular ao ticket quando disponível.

---

## Contexto / Problema

O aplicativo BirdVibe embarca um WebApp via WebView Android. Esse WebApp precisa acionar
a leitura de códigos de barras (boletos) e QR Codes (Pix) diretamente do dispositivo.

O navegador WebView não oferece UX adequada para captura de câmera em ambiente
corporativo controlado; a leitura nativa via ML Kit entrega performance, precisão e
experiência superiores. Sem essa ponte, o usuário seria forçado a digitar linhas
digitáveis manualmente — fluxo sujeito a erro e abandono.

O contrato de comunicação entre o Android e o WebApp já está definido e aprovado em
`specs/ml-kit-scan-bridge.spec.md` (padrão Event-Push via `window.VibeBird.onScanResult`).
Esta PRD cobre **exclusivamente o lado Android** da entrega.

---

## Objetivos

- Disponibilizar uma tela Android que carregue uma URL de localhost configurável.
- Configurar a WebView corretamente para receber chamadas do JS e executar o scanner nativo.
- Integrar o ML Kit Code Scanner (GMS) para leitura de CODE_128 (boleto) e QR_CODE (Pix).
- Empurrar o resultado do scan de volta à WebView via `evaluateJavascript`, respeitando
  o contrato de payload JSON definido na spec.
- Tratar todos os cenários de erro/cancelamento com os códigos padronizados da spec
  (`CAMERA_DENIED`, `NO_BARCODE_FOUND`, `TIMEOUT`, `CANCELLED`, `UNKNOWN_ERROR`).

---

## Usuários Afetados

**Usuário final** — cliente do app BirdVibe que realiza pagamentos de boletos ou
transferências Pix pelo WebApp embutido, em qualquer momento em que a tela de scanner
for acionada pelo WebApp.

**Time de desenvolvimento web** — depende do lado Android estar funcional e respeitando
o contrato para integrar o hook `useScanEvent` no WebApp.

---

## User Stories

- Como **usuário do app**, quero escanear o código de barras de um boleto com a câmera
  do meu celular, para que o valor e a linha digitável sejam preenchidos automaticamente
  no WebApp sem eu precisar digitar.

- Como **usuário do app**, quero escanear um QR Code Pix, para que a chave Pix seja
  lida e enviada de volta ao WebApp de forma instantânea.

- Como **usuário do app**, quero que, ao cancelar ou ocorrer um erro na leitura,
  o WebApp seja informado com um código claro, para que eu receba uma mensagem
  adequada e possa tentar novamente.

---

## Critérios de Aceitação

- [ ] A tela Android abre e carrega a URL de localhost configurada na WebView.
- [ ] A WebView tem JavaScript habilitado e suporte a `evaluateJavascript`.
- [ ] O ML Kit é configurado com `FORMAT_CODE_128` e `FORMAT_QR_CODE`.
- [ ] Scan bem-sucedido de boleto envia payload `{ status:"success", type:"boleto", rawFormat:"CODE_128", value:"..." }`.
- [ ] Scan bem-sucedido de Pix envia payload `{ status:"success", type:"pix", rawFormat:"QR_CODE", value:"..." }`.
- [ ] Cancelamento pelo usuário envia `{ status:"error", code:"CANCELLED" }`.
- [ ] Permissão de câmera negada envia `{ status:"error", code:"CAMERA_DENIED", message:"..." }`.
- [ ] Falha genérica/timeout envia `{ status:"error", code:"UNKNOWN_ERROR" | "TIMEOUT" | "NO_BARCODE_FOUND", message:"..." }`.
- [ ] O guard `window.VibeBird &&` está presente na string JS antes de chamar `onScanResult`.
- [ ] `pushToWebView` é sempre executado na UI thread.
- [ ] `rawFormat` usa strings do contrato (`"CODE_128"`, `"QR_CODE"`), não constantes inteiras do ML Kit.
- [ ] A URL de localhost é configurável (não hardcoded).

---

## Fora do Escopo

- Implementação do lado web/JS — coberto pela `specs/ml-kit-scan-bridge.spec.md`.
- Leitura de outros formatos além de CODE_128 e QR_CODE.
- Autenticação ou HTTPS da URL de localhost.
- Deep links ou navegação entre múltiplas URLs na WebView.
- Persistência de histórico de scans.

---

## Riscos e Dependências

| Item | Tipo | Mitigação |
|---|---|---|
| Permissão `CAMERA` em runtime (Android 6+) | Risco técnico | Solicitar permissão antes de iniciar o scanner; mapear negação para `CAMERA_DENIED` |
| Disponibilidade do Google Play Services (ML Kit GMS) | Dependência externa | ⚠️ suposição — verificar se o target de dispositivos tem GMS |
| URL de localhost acessível no device físico | Risco de ambiente | Configurar `network_security_config` para permitir HTTP em localhost |
| Timing: WebApp pode não ter registrado `window.VibeBird` quando o scan retorna | Risco de integração | Guard `window.VibeBird &&` no JS mitiga; alinhar com time web o momento de registro |
| Versão mínima do Android SDK | Risco técnico | ⚠️ suposição — confirmar `minSdk`; ML Kit GMS exige API 21+ |

---

## Referências

- Contrato completo (payloads, códigos de erro, exemplo Kotlin): `specs/ml-kit-scan-bridge.spec.md`
- Documentação ML Kit Code Scanner: https://developers.google.com/ml-kit/vision/barcode-scanning/code-scanner
