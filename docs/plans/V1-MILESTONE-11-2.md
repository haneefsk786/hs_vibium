# Day 11.2: Logging Implementation Plan

## Goal
Add structured logging for debugging, quiet by default.

## Go Logging

### File: `clicker/internal/log/log.go`

slog-based structured logging:
- JSON format to stderr
- Quiet by default (logs discarded)
- `--verbose` flag enables all logs

```go
log.Debug("message", "key", value)
log.Info("message", "key", value)
log.Warn("message", "key", value)
log.Error("message", "key", value)
```

### CLI Flag

```
-v, --verbose    Enable debug logging
```

### Files Updated

- `cmd/clicker/main.go` - Add `--verbose` flag, setup logging in PersistentPreRun
- `internal/browser/launcher.go` - Log browser launch/close
- `internal/mcp/server.go` - Log incoming requests
- `internal/mcp/handlers.go` - Log tool calls

---

## JS Logging

### File: `clients/javascript/src/utils/debug.ts`

Environment-based logging:
- `VIBIUM_DEBUG=1` enables logs
- JSON format to stderr

```typescript
debug('message', { key: value });
info('message', { key: value });
warn('message', { key: value });
```

### Files Updated

- `src/browser.ts` - Log browser launch
- `src/vibe.ts` - Log navigation and element finding

---

## Usage

```bash
# Go - quiet by default
./bin/clicker navigate https://example.com

# Go - verbose
./bin/clicker navigate https://example.com -v

# JS - quiet by default
node script.js

# JS - verbose
VIBIUM_DEBUG=1 node script.js
```
