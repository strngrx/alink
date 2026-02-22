# AegisLink

Intent-based Android URL safety pipeline. AegisLink intercepts http/https intents, normalizes and sanitizes URLs using ClearURLs rules, classifies domains, and lets users decide whether to proceed. It runs only on intent activation (no background service).

## Features (Alpha)
- URL normalization and ClearURLs sanitization
- Shortlink unshortening (user-safe)
- Domain classification (blacklist/whitelist/implicit trust)
- VirusTotal scan (user-initiated, optional)
- Local stats (cleaned/blocked)
- List management (blacklist/whitelist)
- Onboarding to select forward browser

## Build
This repo is intended for CI builds. A GitHub Actions workflow is included for APK builds.

## Data Seeding
Place initial blacklist domains in `app/src/main/assets/blacklist_seed.txt` (one domain per line). The app seeds this list once on first run.
The current seed list is a minimal snapshot of recent PhishTank submissions.

## Permissions
- `INTERNET`
- `ACCESS_NETWORK_STATE`

## Security Notes
- API keys are stored in `EncryptedSharedPreferences`
- No URL logging (developer logs are opt-in and do not include URL data)

## Limitations
- WebView URLs inside apps are not intercepted
- Hardcoded/explicit intents may bypass AegisLink
- ClearURLs rules are bundled at build time
- VirusTotal free tier limit: 4 req/min

## Attributions
- ClearURLs
- PhishTank
