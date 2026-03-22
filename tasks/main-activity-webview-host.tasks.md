# Tasks: MainActivity WebView Host

> Spec: `specs/main-activity-webview-host.spec.md` — `[x] Approved` `[x] Implemented`
> Status: ✅ Concluído
> Build: ✅ Sucesso (sem erros)

---

**Grupo A — ⚡ Parallelizável (sem dependências entre si):**
- [x] T1: Criar `MainWebViewIntent`, `MainWebViewEffect`, `MainWebViewState` — manual (tipos Kotlin) ✅
- [x] T2: Criar `res/xml/network_security_config.xml` — manual ✅
- [x] T3: Adicionar `buildConfigField("WEBVIEW_BASE_URL", ...)` em `app/build.gradle.kts` — manual ✅

**Grupo B — Após Grupo A:**
- [x] T4: Atualizar `AndroidManifest.xml` (INTERNET + `networkSecurityConfig`) — depende de: T2 — manual ✅
- [x] T5: Criar `MainWebViewViewModel.kt` — depende de: T1, T3 — skill: `new-composable` ✅

**Grupo C — Sequential (após Grupo B completo):**
- [x] T6: Criar `MainWebViewScreen.kt` — depende de: T5 — skill: `new-screen` ✅
- [x] T7: Atualizar `MainActivity.kt` (remover placeholder, chamar `MainWebViewScreen`) — depende de: T6 — manual ✅

**Dependências resumidas:**
```
T1 ──┐
T3 ──┤── T5 ──┐
T2 ── T4      ├── T6 ── T7
```
