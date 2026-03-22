# PRD: WebView Principal — MainActivity como Host da WebApp

## Status
- [x] Draft
- [ ] Approved
- [ ] Specced

## Referência da Task
> Alterar a MainActivity atual para hospedar a WebView que carrega a WebApp via localhost.

---

## Contexto / Problema

Atualmente a `MainActivity` exibe apenas um placeholder Compose ("Hello Android") sem
funcionalidade real. O app BirdVibe precisa que a tela principal seja a própria WebApp,
servida via localhost no dispositivo.

A WebView já existe no projeto como parte do `ScannerActivity` (ponte de scanner), mas a
tela de entrada do app ainda não carrega a WebApp. Sem essa mudança, o usuário não tem
acesso à aplicação principal ao abrir o app.

---

## Objetivos

- Substituir o conteúdo placeholder da `MainActivity` por uma WebView que carrega a URL
  de localhost configurável.
- Garantir que a WebView tenha JavaScript habilitado para que a WebApp funcione.
- Configurar as permissões e o `network_security_config` necessários para tráfego HTTP
  em localhost no Android moderno (cleartext traffic).
- Manter a `MainActivity` como ponto de entrada (`LAUNCHER`) do app.

---

## Usuários Afetados

**Usuário final** — ao abrir o app BirdVibe, deve ver a WebApp carregada, não uma tela
de placeholder. Qualquer usuário com o app instalado é impactado imediatamente.

---

## User Stories

- Como **usuário do app**, quero que ao abrir o app eu veja a interface da WebApp
  carregada, para que eu possa utilizá-la sem etapas adicionais.

- Como **usuário do app**, quero que a WebApp carregue corretamente com JavaScript
  funcional, para que todas as interações (scanner, pagamentos) estejam disponíveis.

- Como **desenvolvedor**, quero que a URL do localhost seja configurável e não
  hardcoded, para facilitar mudanças de porta ou ambiente de desenvolvimento.

---

## Critérios de Aceitação

- [ ] A `MainActivity` carrega a WebView como conteúdo principal ao ser iniciada.
- [ ] A WebView aponta para a URL de localhost configurável (ex: `http://10.0.2.2:3000` para emulador).
- [ ] JavaScript está habilitado na WebView (`javaScriptEnabled = true`).
- [ ] O app possui permissão `INTERNET` no `AndroidManifest.xml`.
- [ ] O `network_security_config` permite tráfego cleartext (HTTP) para localhost.
- [ ] A WebView ocupa toda a área da tela (fullscreen / edge-to-edge).
- [ ] O botão de voltar do Android navega pela pilha de histórico da WebView (se houver histórico), e só sai do app quando não há mais histórico.
- [ ] O código placeholder (`Greeting` composable e `GreetingPreview`) é removido da `MainActivity`.

---

## Fora do Escopo

- Implementação da lógica de abertura do scanner — coberta pela `ScannerActivity` existente e pela `specs/ml-kit-scan-bridge.spec.md`.
- Autenticação ou configuração de HTTPS.
- Suporte a múltiplas URLs ou deep links.
- Exibição de tela de loading/splash customizada durante o carregamento da WebView.
- Tratamento de erros de rede (ex: página de erro customizada quando localhost não está acessível).

---

## Riscos e Dependências

| Item | Tipo | Mitigação |
|---|---|---|
| Localhost não acessível no emulador | Risco de ambiente | Usar `10.0.2.2` para emulador Android; `localhost` para dispositivo físico via USB debugging |
| Tráfego HTTP bloqueado (Android 9+) | Risco técnico | Adicionar `network_security_config` com `cleartextTrafficPermitted` para localhost |
| WebApp JS não carregado ao abrir scanner | Risco de integração | Depende de `window.VibeBird` estar registrado — já mitigado pelo guard na `ScannerActivity` |
| Compatibilidade mínima do SDK | Risco técnico | `minSdk` é 28 (Android 9) — `network_security_config` é obrigatório nessa versão |

---

## Referências

- `app/src/main/java/com/example/webviewscannerbirdvibe/MainActivity.kt` — arquivo a ser modificado
- `app/src/main/java/com/example/webviewscannerbirdvibe/scanner/ScannerActivity.kt` — padrão de WebView existente no projeto
- `app/src/main/AndroidManifest.xml` — permissões e config
- `specs/ml-kit-scan-bridge.spec.md` — contrato de comunicação WebApp ↔ Scanner (já aprovado e implementado)
