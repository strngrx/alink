# AegisLink — Alpha TODO

> Package: `com.aana.aegislink`  
> Target: Android 8.0+ (API 26), Target SDK 34  
> Release: GitHub APK (sideload)

---

## Phase 0 — Project Setup
- [ ] Init Android project (Kotlin, minSdk 26, targetSdk 34)
- [ ] Configure `AndroidManifest.xml` with `ACTION_VIEW` intent filter for `http`/`https`
- [ ] Set up package structure (`core`, `shortlink`, `api`, `db`, `ui`, `utils`)
- [ ] Add dependencies: Room, OkHttp/Retrofit, EncryptedSharedPreferences, Material3
- [ ] Bundle `clearurls_rules.json` in `res/raw/`
- [ ] Bundle `shorteners_list.json` in `res/raw/`
- [ ] Seed initial blacklist DB (~5k–10k entries from PhishTank + malware domains)
- [ ] Set up Git repo, `.gitignore` (exclude API keys, build artifacts)

---

## Phase 1 — Core Pipeline
- [ ] `UrlNormalizer` — lowercase domain, percent-decode, strip fragments, remove trailing slash
- [ ] `UrlSanitizer` — parse ClearURLs rules, strip tracking params, preserve functional params, extract implicit trust signal
- [ ] `UrlClassifier` — decision tree: blacklist → implicit trust → whitelist → UNKNOWN
- [ ] `UrlPipeline` — orchestrate full chain, end-to-end <200ms for local ops
- [ ] Unit test each stage independently

---

## Phase 2 — Database Layer
- [ ] `AegisDatabase` — Room DB setup with all 5 tables
- [ ] `BlacklistDao` — indexed domain lookup, target <15ms
- [ ] `WhitelistDao` — user-managed trusted domains
- [ ] `SettingsDao` — key-value store for all toggles + preferences
- [ ] `StatisticsDao` — aggregate counters only, never individual URLs
- [ ] `VtCacheDao` — store scan results by URL hash, 24hr TTL, auto-prune on access

---

## Phase 3 — Shortlink Resolution
- [ ] `ShortlinkDetector` — match normalized URL against `shorteners_list.json`
- [ ] `UnshortenService` — POST to `unshorten.me/api/v2/unshorten`, 10s timeout, single retry
- [ ] Graceful fallback if API fails — surface unresolved URL with warning to user
- [ ] Feed resolved URL back into full pipeline from top (don't skip normalization)

---

## Phase 4 — VirusTotal Integration
- [ ] `VirusTotalService` — POST URL to VT API v3, poll GET for results, parse engine breakdown
- [ ] `SecurityKeyStore` — store API key in `EncryptedSharedPreferences`, never log it
- [ ] `VerdictClassifier` — derive single label from engine counts:
  - `0` malicious → **UNDETECTED**
  - `1–3` malicious or any suspicious → **SUSPICIOUS**
  - `4+` malicious → **DANGEROUS**
  - Not scanned → **UNKNOWN**
- [ ] Handle rate limit (4 req/min), invalid key, timeout (15s), network errors gracefully
- [ ] Wire VT toggle in settings — grey out API key field when toggle is off

---

## Phase 5 — UI
- [ ] `InterceptorActivity` — receive intent, run pipeline, route to correct dialog/auto-proceed
- [ ] `MaliciousUrlDialog` — blocked URL bottom sheet (URL + reason + Close + Report False Positive)
- [ ] `UnknownUrlDialog` — decision bottom sheet:
  - URL display
  - VT scan status section (engine breakdown + verdict label)
  - Actions: **Scan with VirusTotal** (primary), **Proceed anyway** (de-emphasized), **Close** (text), **Add to blacklist**, **Add to whitelist**
  - Post-scan: update dialog in-place, don't navigate away
- [ ] `ListManagerActivity` — view/add/remove entries for blacklist and whitelist
- [ ] `SettingsActivity` — all toggles with accurate descriptions:
  - URL Sanitization
  - Allow referral parameters
  - Auto Blacklist
  - VirusTotal Scan + API key input
  - Instant Trust — fix subtitle to *"Auto-proceed implicitly trusted links"* (not "whitelisted")
- [ ] `MainActivity` — dashboard:
  - Status card (ENABLED/DISABLED + "Active as default browser")
  - Cleaned / Blocked counters with daily delta
  - Protection settings section
  - Theme toggle
- [ ] Dark / light theme — full consistency across all screens
- [ ] Material3 design throughout

---

## Phase 6 — List Management & Stats
- [ ] Add/remove entries from blacklist and whitelist via UI
- [ ] Auto-blacklist on block toggle (FR-3.7.5) — with confirmation, not silent
- [ ] Auto-whitelist on proceed toggle (FR-3.7.6):
  - **"Proceed once"** — no list modification
  - **"Always allow this domain"** — explicit whitelist add with confirmation
- [ ] Stats counters persist across restarts
- [ ] Stats reset when user clears app data

---

## Phase 7 — Polish & Release
- [ ] Onboarding screen on first launch:
  - Set AegisLink as default browser prompt
  - One-liner limitations disclaimer (WebView, hardcoded intents, no 100% guarantee)
- [ ] About screen — version, GitHub link, open source attributions (ClearURLs, PhishTank)
- [ ] Performance profiling — validate all SRS targets met
- [ ] Crash-free rate testing, graceful degradation for all API failures
- [ ] Build signed APK
- [ ] Write `README.md` — setup, permissions, known limitations
- [ ] Publish to GitHub releases

---

## Known Limitations (won't fix in alpha)
- WebView URLs (in-app browsers) are not intercepted — OS limitation
- Hardcoded/explicit intents (Google apps, Chrome, Play Store, WhatsApp) bypass AegisLink entirely
- No 100% safety guarantee — UNDETECTED ≠ SAFE
- VirusTotal free tier: 4 req/min, user must supply own API key
- ClearURLs rules bundled at build time — no live updates in alpha

---

## Post-Alpha Ideas
- [ ] Context-aware "Proceed anyway" button label based on scan verdict
- [ ] In-app WebView mode (optional) for full interception coverage
- [ ] ClearURLs rule auto-updates
- [ ] StevenBlack host file import
- [ ] Play Store release
