# day 12: published to npm 🚀🎉📦

**date:** 2025-12-22

vibium v0.1.2 is live on npm. three days before christmas. 🎄

## what got published

six packages, all at v0.1.2:

- `vibium` - main package (re-exports JS client, runs postinstall)
- `@vibium/darwin-arm64` - macOS Apple Silicon
- `@vibium/darwin-x64` - macOS Intel
- `@vibium/linux-arm64` - Linux ARM
- `@vibium/linux-x64` - Linux x64
- `@vibium/win32-x64` - Windows x64

install with:

```bash
npm install vibium
```

chrome for testing downloads automatically on first install.

## docs overhaul

made all JS examples REPL-friendly:

```javascript
// Option 1: require (REPL-friendly)
const { browserSync } = require('vibium')

// Option 2: dynamic import (REPL with --experimental-repl-await)
const { browser } = await import('vibium')

// Option 3: static import (in .mjs or .ts files)
import { browser, browserSync } from 'vibium'
```

updated README, CONTRIBUTING, and npm-publishing guides with:
- all three import options
- both sync and async examples
- realistic selectors (example.com's actual `<a>` tag)
- idiomatic file writing (sync: `fs`, async: `fs/promises`)

## visible by default

we flipped the default from headless to visible. now when you run:

```javascript
const vibe = browserSync.launch()
```

you see the browser. no flags needed. this optimizes for the "aha!" moment when someone tries vibium for the first time.

to hide the browser, explicitly pass `headless: true` or use `--headless` on the CLI.

## CLI flag change

```bash
# old (no longer exists)
clicker screenshot https://example.com --headed

# new
clicker screenshot https://example.com --headless
```

the flag flipped because the default flipped. visible is now the baseline.

## new make targets

```bash
make install-browser  # Download Chrome for Testing
make package          # Build all npm packages
make clean-packages   # Clean built packages
```

## what's next

three days until christmas. the V1 goal is in sight:
- MCP server works with Claude Code
- JS client (sync + async) works
- CLI works
- npm packages published

next: final polish, maybe a demo video? definitely some memes.

✨🎅🎄🎁✨

*december 22, 2025*
