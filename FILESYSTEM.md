# Vibium V1 File System Layout

```
vibium/
│
├── README.md
├── LICENSE
├── V1-ROADMAP.md
├── V2-ROADMAP.md
├── .gitignore
├── package.json                          # npm workspaces root
│
├── clicker/                              # Go Core Engine
│   ├── go.mod
│   ├── go.sum
│   │
│   ├── cmd/
│   │   └── clicker/
│   │       └── main.go                   # CLI entry point (cobra)
│   │
│   ├── internal/
│   │   ├── config/
│   │   │   └── config.go                 # Configuration management
│   │   │
│   │   ├── bidi/                         # WebDriver BiDi Protocol
│   │   │   ├── connection.go             # WebSocket connection (gorilla)
│   │   │   ├── protocol.go               # BiDi message types
│   │   │   ├── session.go                # session module
│   │   │   ├── browsingcontext.go        # browsingContext module
│   │   │   ├── script.go                 # script module
│   │   │   ├── input.go                  # input module
│   │   │   └── element.go                # Element finding via script
│   │   │
│   │   ├── proxy/                        # BiDi Pass-through Proxy
│   │   │   ├── server.go                 # WebSocket proxy server
│   │   │   └── router.go                 # Message routing (goroutines)
│   │   │
│   │   ├── browser/                      # Browser Management
│   │   │   ├── launcher.go               # Browser process launching
│   │   │   └── installer.go              # Chrome for Testing downloader
│   │   │
│   │   ├── features/                     # High-level Features
│   │   │   └── autowait.go               # Auto-waiting logic
│   │   │
│   │   ├── mcp/                          # MCP Server Interface
│   │   │   ├── server.go                 # MCP server (stdio JSON-RPC)
│   │   │   ├── handlers.go               # MCP tool handlers
│   │   │   └── schema.go                 # Tool schemas
│   │   │
│   │   ├── paths/
│   │   │   └── paths.go                  # Platform-specific paths
│   │   │
│   │   ├── process/
│   │   │   └── process.go                # Process management
│   │   │
│   │   └── errors/
│   │       └── errors.go                 # Custom error types
│   │
│   ├── pkg/                              # Public packages (if needed)
│   │   └── version/
│   │       └── version.go
│   │
│   └── bin/                              # Build output (gitignored)
│       ├── clicker-linux-amd64
│       ├── clicker-linux-arm64
│       ├── clicker-darwin-amd64
│       ├── clicker-darwin-arm64
│       └── clicker-windows-amd64.exe
│
├── clients/
│   │
│   └── javascript/                       # JS/TS Client Library
│       ├── package.json
│       ├── tsconfig.json
│       ├── tsup.config.ts                # Build config
│       ├── vitest.config.ts              # Test config
│       │
│       ├── src/
│       │   ├── index.ts                  # Main exports
│       │   ├── browser.ts                # browser.launch() entry (async)
│       │   ├── vibe.ts                   # Vibe class (async API)
│       │   ├── element.ts                # Element class (async)
│       │   │
│       │   ├── sync/                     # Sync API Wrappers
│       │   │   ├── index.ts              # Sync exports
│       │   │   ├── bridge.ts             # Worker thread bridge (Atomics.wait)
│       │   │   ├── worker.ts             # Worker thread (runs async ops)
│       │   │   ├── browser.ts            # browserSync.launch()
│       │   │   ├── vibe.ts               # VibeSync class
│       │   │   └── element.ts            # ElementSync class
│       │   │
│       │   ├── clicker/                  # Clicker Binary Management
│       │   │   ├── binary.ts             # Binary path resolution
│       │   │   ├── process.ts            # Spawn & manage clicker
│       │   │   └── platform.ts           # Platform detection
│       │   │
│       │   ├── bidi/                     # BiDi Client
│       │   │   ├── connection.ts         # WebSocket connection
│       │   │   ├── client.ts             # BiDi command client
│       │   │   └── types.ts              # BiDi type definitions
│       │   │
│       │   └── utils/
│       │       ├── errors.ts             # Error classes
│       │       └── timeout.ts            # Promise timeout wrapper
│       │
│       └── test/
│           ├── browser.test.ts
│           ├── vibe.test.ts
│           └── element.test.ts
│
├── packages/                             # Platform-specific npm packages
│   ├── vibium/                           # Main package (re-exports)
│   │   ├── package.json
│   │   ├── bin.js                        # npx entry → execs clicker mcp
│   │   └── postinstall.js                # Runs clicker install for Chrome
│   ├── vibium-linux-x64/
│   │   └── package.json
│   ├── vibium-linux-arm64/
│   │   └── package.json
│   ├── vibium-darwin-x64/
│   │   └── package.json
│   ├── vibium-darwin-arm64/
│   │   └── package.json
│   └── vibium-win32-x64/
│       └── package.json
│
├── docs/
│   ├── getting-started.md
│   ├── api.md
│   └── updates/
│       └── 2025-12-11-v1-announcement.txt
│
├── examples/
│   ├── async-basic/
│   │   ├── package.json
│   │   └── index.ts
│   ├── sync-basic/
│   │   ├── package.json
│   │   └── index.ts
│   └── claude-code-mcp/
│       └── README.md
│
└── scripts/
    ├── build-clicker.sh                  # Go cross-compile
    └── package-npm.sh                    # Package for npm publish
```
