# Vibium

Browser automation for AI agents and humans.

## Installation

```bash
npm install vibium
```

This automatically downloads Chrome for Testing on first install.

## Quick Start

### Async API

```javascript
import { browser } from 'vibium'
import { writeFile } from 'fs/promises'

const bro = await browser.start()
const vibe = await bro.page()
await vibe.go('https://example.com')

const link = await vibe.find('a')
console.log(await link.text())
await link.click()

const screenshot = await vibe.screenshot()
await writeFile('screenshot.png', screenshot)

await bro.stop()
```

### Sync API

```javascript
const { browser } = require('vibium/sync')
const { writeFileSync } = require('fs')

const bro = browser.start()
const vibe = bro.page()
vibe.go('https://example.com')

const link = vibe.find('a')
console.log(link.text())
link.click()

const screenshot = vibe.screenshot()
writeFileSync('screenshot.png', screenshot)

bro.stop()
```

## Environment Variables

| Variable | Description |
|----------|-------------|
| `VIBIUM_SKIP_BROWSER_DOWNLOAD` | Set to `1` to skip Chrome download on install |

## Requirements

- Node.js 18+

## Links

- [GitHub / Documentation](https://github.com/VibiumDev/vibium)
- [Website](https://vibium.com)

## License

Apache-2.0
