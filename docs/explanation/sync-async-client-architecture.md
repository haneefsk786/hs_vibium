# Sync/Async Client Architecture

How Vibium's JavaScript client exposes the same browser automation API as both async (`await page.go(url)`) and sync (`page.go(url)`) calls, using a worker thread and SharedArrayBuffer to block the main thread while async work runs off-thread.

## When to use this pattern

Use this when your library is inherently async (network I/O, child processes, WebSocket protocols) but consumers need a synchronous API — e.g. scripts run by AI agents, REPL tools, or test harnesses that don't want `await` everywhere.

The tradeoff: you get a clean sync DX at the cost of a worker thread, a bundled worker entry, and SharedArrayBuffer (which requires Node.js 16+ and, in browsers, cross-origin isolation headers — though this pattern is Node-only).

## Architecture overview

Four layers, bottom to top:

```
┌─────────────────────────────────────────┐
│  User code (sync)                       │
│  const b = browser.start();             │
│  const page = b.page();                 │
│  page.go('https://example.com');        │
├─────────────────────────────────────────┤
│  Sync Client Classes                    │  main thread
│  BrowserSync, PageSync, ElementSync     │
│  ─ thin wrappers: bridge.call(method)   │
├─────────────────────────────────────────┤
│  SyncBridge                             │  main thread
│  ─ SharedArrayBuffer + Atomics.wait()   │
│  ─ blocks main thread until result      │
│  ─ handles mid-call callbacks           │
╞═════════════════════════════════════════╡
│  Worker Thread                          │  worker thread
│  ─ imports the real async classes        │
│  ─ object registry (pages, elements)    │
│  ─ dispatch table: method → handler     │
│  ─ event buffering                      │
├─────────────────────────────────────────┤
│  Async Client Classes                   │  worker thread
│  Browser, Page, Element (the real API)  │
└─────────────────────────────────────────┘
```

Data flow for `page.go('https://example.com')`:

1. `PageSync.go(url)` calls `bridge.call('page.go', [pageId, url])`
2. Bridge sends `{ method: 'page.go', args: [pageId, url] }` to worker via `postMessage`
3. Bridge calls `Atomics.wait(signal, 0, 0)` — main thread blocks
4. Worker looks up `handlers['page.go']`, calls `await getPage(pageId).go(url)`
5. Worker posts result to the per-call MessagePort, sets `signal[0] = 1`, calls `Atomics.notify`
6. Main thread wakes, reads result from the port, returns it

## The SyncBridge

`clients/javascript/src/sync/bridge.ts` (263 lines)

### Signal protocol

A 2-slot `SharedArrayBuffer` (8 bytes) carries the coordination signals:

| Slot | Direction | Values |
|------|-----------|--------|
| `signal[0]` | worker -> main | `0` = idle, `1` = result ready, `2` = callback needed |
| `signal[1]` | main -> worker | `0` = idle (slot reserved for future use) |

The worker writes `signal[0]`; the main thread reads it. The main thread blocks with `Atomics.wait(signal, 0, 0)` — it sleeps until the value is no longer `0`.

### Bridge creation

```ts
static create(): SyncBridge {
  // 2 slots: signal[0] = worker→main, signal[1] = main→worker
  const signal = new Int32Array(new SharedArrayBuffer(8));

  // Persistent callback channel (for events/callbacks from worker)
  const { port1: callbackPortMain, port2: callbackPortWorker } = new MessageChannel();

  const workerPath = path.join(__dirname, 'worker.js');

  const worker = new Worker(workerPath, {
    workerData: { signal, callbackPort: callbackPortWorker },
    transferList: [callbackPortWorker],
  });

  return new SyncBridge(worker, signal, callbackPortMain, callbackPortWorker);
}
```

Key details:
- `SharedArrayBuffer` is passed in `workerData` — both threads share the same memory.
- `callbackPort` is transferred (not cloned) to the worker. The main thread keeps its end (`callbackPortMain`) for receiving callback requests.
- The worker file path uses `__dirname` so it works for both dev (ts-node) and built (dist/) scenarios.

### The `call()` method

This is the core blocking call. Annotated:

```ts
call<T = unknown>(method: string, args: unknown[] = []): T {
  // 1. Drain any callbacks that arrived between calls
  this.processPendingCallbacks();

  const cmd = { id: this.commandId++, method, args };

  // 2. Create a one-shot MessageChannel for this call's result
  //    (avoids multiplexing issues — each call gets its own port)
  const { port1, port2 } = new MessageChannel();

  // 3. Reset signals
  Atomics.store(this.signal, 0, 0);
  Atomics.store(this.signal, 1, 0);

  // 4. Send command + port to worker
  this.worker.postMessage({ cmd, port: port2 }, [port2]);

  // 5. Block until worker signals
  const commandTimeoutMs = 60_000;
  const waitSliceMs = 1000;
  const startTime = Date.now();

  for (;;) {
    const waitResult = Atomics.wait(this.signal, 0, 0, waitSliceMs);

    if (waitResult === 'timed-out') {
      if (Date.now() - startTime >= commandTimeoutMs) {
        port1.close();
        throw new Error(`Bridge call '${method}' timed out after 60s`);
      }
      continue; // worker still alive, keep waiting
    }

    const sig = Atomics.load(this.signal, 0);

    if (sig === 1) {
      // Result ready — read from the per-call port
      const message = receiveMessageOnPort(port1);
      port1.close();
      if (!message) throw new Error('No response from worker');
      const response = message.message as CommandResult;
      if (response.error) throw new Error(response.error);
      return response.result as T;
    }

    if (sig === 2) {
      // Worker needs a callback handled on the main thread
      this.handleCallback();
    }
  }
}
```

Why a per-call `MessageChannel` instead of a single shared channel:
- `receiveMessageOnPort()` is synchronous — it pulls one message from the port's internal queue.
- With a shared channel, messages from different calls could interleave.
- Per-call ports guarantee the first (and only) message on that port is the result for *this* call.

Why `Atomics.wait` with a 1-second slice instead of infinite timeout:
- Allows checking total elapsed time for a 60s hard timeout.
- Prevents permanent hangs if the worker dies silently.

### Callback handling (main thread side)

When `signal[0] === 2`, the worker needs the main thread to run a callback (e.g., a route handler, dialog handler, or event listener). The protocol:

1. Worker posts `{ handlerId, data }` to `callbackPort`, then sets `signal[0] = 2` and notifies.
2. Main thread wakes from `Atomics.wait`, sees `sig === 2`, calls `handleCallback()`.
3. `handleCallback()` reads the message from `callbackPortMain` via `receiveMessageOnPort()`.
4. Looks up the handler by ID, calls it, gets a decision.
5. Resets `signal[0] = 0` **before** posting the decision back (critical ordering — see below).
6. Posts `{ decision }` back on `callbackPortMain`.

```ts
private handleCallback(): void {
  // Spin-wait for port message (postMessage is async, may lag behind Atomics)
  let cbMsg = receiveMessageOnPort(this.callbackPortMain);
  let spinCount = 0;
  while (!cbMsg) {
    spinCount++;
    if (spinCount >= 60_000) {
      throw new Error('Timed out waiting for callback message from worker (60s)');
    }
    Atomics.wait(this.signal, 1, Atomics.load(this.signal, 1), 1); // 1ms sleep
    cbMsg = receiveMessageOnPort(this.callbackPortMain);
  }

  let decision: unknown = null;
  const req = cbMsg.message as CallbackRequest;
  const handler = this.handlers.get(req.handlerId);
  if (handler) {
    try { decision = handler(req.data); } catch { /* default null */ }
  }

  // CRITICAL: Reset signal[0] BEFORE posting decision.
  // If we reset after, the worker might receive the decision, prepare the
  // next callback, and set signal[0]=2 before our reset — overwriting it
  // back to 0 and causing a deadlock.
  Atomics.store(this.signal, 0, 0);
  this.callbackPortMain.postMessage({ decision });
}
```

### Cleanup

The bridge registers process-level handlers for `exit`, `SIGINT`, `SIGTERM` to ensure the worker (and the browser it controls) is cleaned up:

```ts
const activeBridges: Set<SyncBridge> = new Set();

process.on('exit', cleanup);
process.on('SIGINT', () => { cleanup(); process.exit(130); });
process.on('SIGTERM', () => { cleanup(); process.exit(143); });
```

`tryQuit()` sends a `quit` command to the worker (which calls `browser.stop()`) with a 5-second timeout, then terminates the worker. `terminate()` is the hard kill.

## The Worker

`clients/javascript/src/sync/worker.ts` (1409 lines)

### Object registry

The worker can't send live objects (Page, Element) back to the main thread — they contain WebSocket connections, event listeners, etc. Instead, it keeps an integer-keyed registry:

```ts
let browserInstance: Browser | null = null;
let nextId = 1;
const pages = new Map<number, Page>();
const contexts = new Map<number, BrowserContext>();
const elements = new Map<number, Element>();

function storePage(page: Page): number {
  const id = nextId++;
  pages.set(id, page);
  return id;
}

function getPage(id: number): Page {
  const p = pages.get(id);
  if (!p) throw new Error(`Page ${id} not found`);
  return p;
}
```

The main thread only ever sees integer IDs. Sync wrapper classes store the ID and pass it back with every call.

### Dispatch table

A flat `Record<string, Handler>` maps method names to async handlers:

```ts
const handlers: Record<string, Handler> = {
  'browser.start': async (args) => {
    const [url, options] = args as [string | undefined, StartOptions | undefined];
    browserInstance = await browser.start(url, options);
    const page = await browserInstance.page();
    defaultPageId = storePage(page);
    return { pageId: defaultPageId };
  },

  'page.go': async (args) => {
    const [pageId, url] = args as [number, string];
    await getPage(pageId).go(url);
    return { success: true };
  },

  'element.click': async (args) => {
    const [elementId, options] = args as [number, ActionOptions | undefined];
    await getElement(elementId).click(options);
    return { success: true };
  },

  // ... ~100 more handlers
};
```

Pattern for each handler:
1. Destructure `args` with a type assertion.
2. Look up the real object from the registry.
3. `await` the async method.
4. Return a plain serializable object (no class instances, no functions).

For methods that return objects (Page, Element), store the object and return its ID:

```ts
'page.find': async (args) => {
  const [pageId, selector, options] = args as [number, string | SelectorOptions, FindOptions | undefined];
  const el = await getPage(pageId).find(selector, options);
  return { elementId: storeElement(el), info: el.info };
},
```

### Event buffering

Events (new page, network request, etc.) can fire *during* a command. If the worker immediately signals the main thread with `signal[0] = 2` for a callback while the main thread is waiting for `signal[0] = 1` (result), there's no race — the bridge loop handles both. But delivering events *during* the command can cause confusion (the user's callback runs before the command returns).

Solution: buffer events during command execution, drain them after the command completes but before signaling the result:

```ts
const pageEventBuffer: Array<{ type: 'page' | 'popup'; pageId: number }> = [];
const networkEventBuffer: Array<{ handlerId: string; data: unknown }> = [];

// In event listeners (set up by 'browser.onPage', 'page.onRequestWithCallback', etc.):
browserInstance.onPage((page) => {
  const id = storePage(page);
  pageEventBuffer.push({ type: 'page', pageId: id });
});

// In the message handler, AFTER the command completes:
parentPort!.on('message', async ({ cmd, port }) => {
  let result, error;
  try { result = await handleCommand(cmd); }
  catch (err) { error = err.message; }

  // Yield so pending BiDi events are processed
  if (onPageHandlerId || onPopupHandlerId || networkEventBuffer.length > 0) {
    await new Promise(r => setImmediate(r));
  }

  // Drain page/popup events
  while (pageEventBuffer.length > 0) {
    const event = pageEventBuffer.shift()!;
    const handlerId = event.type === 'page' ? onPageHandlerId : onPopupHandlerId;
    if (handlerId) {
      await invokeMainThread(handlerId, { pageId: event.pageId });
    }
  }

  // Drain network events
  while (networkEventBuffer.length > 0) {
    const event = networkEventBuffer.shift()!;
    await invokeMainThread(event.handlerId, event.data);
  }

  // NOW send the result
  port.postMessage({ result, error });
  Atomics.store(signal, 0, 1);
  Atomics.notify(signal, 0);
});
```

### Worker-to-main callbacks (`invokeMainThread`)

When the worker needs a decision from the main thread (e.g., a route handler deciding whether to continue or abort a request):

```ts
let callbackChain = Promise.resolve<unknown>(undefined);

function invokeMainThread(handlerId: string, data: unknown): Promise<unknown> {
  const call = callbackChain.then(async () => {
    // Set up listener BEFORE signaling (avoids race)
    const decisionPromise = new Promise<unknown>((resolve) => {
      callbackPort.once('message', (msg: { decision: unknown }) => {
        resolve(msg.decision);
      });
    });

    callbackPort.postMessage({ handlerId, data });

    Atomics.store(signal, 0, 2);
    Atomics.notify(signal, 0);

    return await decisionPromise;
  });
  // Chain: serialize concurrent callbacks
  callbackChain = call.then(() => {}, () => {});
  return call;
}
```

The `callbackChain` mutex ensures only one callback is in flight at a time — the main thread can only handle one `signal[0] = 2` at a time.

## Sync client wrappers

Each async class gets a 1:1 sync counterpart. The pattern is mechanical:

### Async class (the real implementation)

```ts
// browser.ts
export class Browser {
  async page(): Promise<Page> {
    const result = await this.client.send('vibium:browser.page', {});
    return new Page(this.client, result.context, result.userContext);
  }

  async newPage(): Promise<Page> { ... }
  async stop(): Promise<void> { ... }
}
```

### Sync wrapper

```ts
// sync/browser.ts
export class BrowserSync {
  readonly _bridge: SyncBridge;

  constructor(bridge: SyncBridge) {
    this._bridge = bridge;
  }

  page(): PageSync {
    const result = this._bridge.call<{ pageId: number }>('browser.page');
    return new PageSync(this._bridge, result.pageId);
  }

  newPage(): PageSync {
    const result = this._bridge.call<{ pageId: number }>('browser.newPage');
    return new PageSync(this._bridge, result.pageId);
  }

  stop(): void {
    this._bridge.tryQuit();
  }
}
```

Rules for sync wrappers:
- No `async`, no `Promise` in return types.
- Store `_bridge` and any registry ID (e.g., `_pageId`).
- Every method calls `this._bridge.call<ReturnType>(methodName, [args...])`.
- When the result contains an object ID, wrap it in the corresponding sync class.
- Callback-accepting methods (e.g., `onPage`, `route`, `onDialog`) register a handler on the bridge and pass the handler ID to the worker.

### Entry point

```ts
// sync/browser.ts
export const browser = {
  start(urlOrOptions?: string | StartOptions, options: StartOptions = {}): BrowserSync {
    const bridge = SyncBridge.create();
    try {
      bridge.call('browser.start', [url, options]);
    } catch (e) {
      bridge.terminate();
      throw e;
    }
    return new BrowserSync(bridge);
  },
};
```

Users import from the sync subpath:

```ts
import { browser } from 'vibium-client/sync';
const b = browser.start();
```

vs the async default:

```ts
import { browser } from 'vibium-client';
const b = await browser.start();
```

## Callback/event handling

The pattern supports bidirectional main<->worker communication for three categories:

### 1. Decision callbacks (worker asks main for a decision)

Used for route handlers and dialog handlers where the main thread callback's return value controls what happens:

```ts
// Sync wrapper (main thread)
page.route('**/*.css', (route) => {
  route.abort();  // sets route._decision = { action: 'abort' }
});
// The handler returns route._decision, which the bridge sends back to the worker

// Worker: uses the decision to fulfill/abort the network request
'page.routeWithCallback': async (args) => {
  const [pageId, pattern, handlerId] = args;
  getPage(pageId).route(pattern, async (route) => {
    const decision = await invokeMainThread(handlerId, { url, method, ... });
    // Apply decision to the real route object
  });
},
```

### 2. Fire-and-forget notifications (worker notifies main)

Used for `onRequest`, `onResponse`, `onDownload` — the callback runs on the main thread but its return value is ignored:

```ts
// Main thread handler returns null
this._bridge.registerHandler(handlerId, (data: RequestData) => {
  fn(data);      // user's callback
  return null;   // no decision needed
});
```

### 3. Buffered events (delivered between commands)

Page creation events (`onPage`, `onPopup`) and network events buffer during command execution and drain after — see the event buffering section above.

## Build and package setup

### Three build entries (`tsup.config.ts`)

```ts
import { defineConfig } from "tsup";

export default defineConfig([
  // 1. Main async entry — CJS + ESM + .d.ts
  {
    entry: ["src/index.ts"],
    format: ["cjs", "esm"],
    dts: true,
    clean: true,
  },
  // 2. Sync subpath entry — CJS + ESM + .d.ts
  {
    entry: { sync: "src/sync/index.ts" },
    format: ["cjs", "esm"],
    dts: true,
    outDir: "dist",
    clean: false,
  },
  // 3. Worker — CJS only, all deps bundled
  {
    entry: ["src/sync/worker.ts"],
    format: ["cjs"],
    outDir: "dist",
    clean: false,
    noExternal: [/.*/],  // Bundle EVERYTHING — worker must be self-contained
  },
]);
```

Why the worker is CJS-only with `noExternal: [/.*/]`:
- `new Worker(path)` loads a single file — it can't resolve `node_modules` from the consumer's project.
- Bundling all dependencies into one `worker.js` makes the worker self-contained.
- CJS because `worker_threads` Worker defaults to CJS.

### Subpath exports (`package.json`)

```json
{
  "exports": {
    ".": {
      "types": "./dist/index.d.ts",
      "require": "./dist/index.js",
      "import": "./dist/index.mjs"
    },
    "./sync": {
      "types": "./dist/sync.d.ts",
      "require": "./dist/sync.js",
      "import": "./dist/sync.mjs"
    }
  }
}
```

- `import { browser } from 'vibium-client'` — async API
- `import { browser } from 'vibium-client/sync'` — sync API
- Both export the same shape (`browser.start()` returns Browser/BrowserSync), so code is nearly identical except for `await`.

### Worker path resolution

The bridge resolves the worker at runtime:

```ts
const workerPath = path.join(__dirname, 'worker.js');
```

This works because:
- In the built package, `bridge.js` and `worker.js` are both in `dist/`.
- `__dirname` in CJS resolves to the directory containing the running file.
- For ESM builds, tsup compiles `__dirname` to the equivalent `import.meta.url`-based path.

## Implementation guide

Step-by-step for applying this pattern to a new project.

### Prerequisites

- An existing async API you want to wrap with sync equivalents.
- Node.js 16+ (for `SharedArrayBuffer` and `worker_threads`).
- A bundler that can produce a self-contained worker file (tsup, esbuild, webpack).

### Step 1: Create the SyncBridge

Copy the bridge almost verbatim. The only things to customize:

- **Timeout values** (currently 60s for commands, 5s for quit).
- **Cleanup logic** in `tryQuit()` — what needs to happen when your process exits.

```ts
// src/sync/bridge.ts
import { Worker, MessageChannel, receiveMessageOnPort, MessagePort } from 'worker_threads';
import * as path from 'path';

export class SyncBridge {
  private worker: Worker;
  private signal: Int32Array;
  private commandId = 0;
  private terminated = false;
  private callbackPortMain: MessagePort;
  private handlers = new Map<string, Function>();

  static create(): SyncBridge {
    const signal = new Int32Array(new SharedArrayBuffer(8));
    const { port1, port2 } = new MessageChannel();
    const worker = new Worker(path.join(__dirname, 'worker.js'), {
      workerData: { signal, callbackPort: port2 },
      transferList: [port2],
    });
    return new SyncBridge(worker, signal, port1);
  }

  call<T>(method: string, args: unknown[] = []): T {
    // Reset signal, send command, Atomics.wait loop
    // Handle sig === 1 (result) and sig === 2 (callback)
    // See full implementation above
  }

  registerHandler(id: string, handler: Function): void { ... }
  unregisterHandler(id: string): void { ... }
  tryQuit(): void { ... }
  terminate(): void { ... }
}
```

### Step 2: Create the worker with a dispatch table

```ts
// src/sync/worker.ts
import { parentPort, workerData, MessagePort } from 'worker_threads';
import { YourAsyncClient } from '../client'; // your real async API

const { signal, callbackPort } = workerData;

// Object registry
const objects = new Map<number, any>();
let nextId = 1;
function store(obj: any): number { const id = nextId++; objects.set(id, obj); return id; }
function get(id: number): any { return objects.get(id) ?? throw new Error(`Not found: ${id}`); }

// Dispatch table
const handlers: Record<string, (args: unknown[]) => Promise<unknown>> = {
  'client.connect': async (args) => {
    const client = await YourAsyncClient.connect(args[0] as string);
    return { clientId: store(client) };
  },
  'client.doThing': async (args) => {
    const [clientId, param] = args as [number, string];
    const result = await get(clientId).doThing(param);
    return { value: result };
  },
  // ...
};

// Message loop
parentPort!.on('message', async ({ cmd, port }) => {
  let result, error;
  try {
    const handler = handlers[cmd.method];
    if (!handler) throw new Error(`Unknown: ${cmd.method}`);
    result = await handler(cmd.args);
  } catch (err) {
    error = err instanceof Error ? err.message : String(err);
  }
  port.postMessage({ result, error });
  Atomics.store(signal, 0, 1);
  Atomics.notify(signal, 0);
});
```

### Step 3: Create sync wrapper classes

For each async class, create a sync mirror:

```ts
// src/sync/client.ts
import { SyncBridge } from './bridge';

export class ClientSync {
  constructor(private bridge: SyncBridge, private clientId: number) {}

  doThing(param: string): string {
    const result = this.bridge.call<{ value: string }>('client.doThing', [this.clientId, param]);
    return result.value;
  }
}
```

### Step 4: Configure the build

```ts
// tsup.config.ts
export default defineConfig([
  // Main async entry
  { entry: ["src/index.ts"], format: ["cjs", "esm"], dts: true, clean: true },
  // Sync subpath
  { entry: { sync: "src/sync/index.ts" }, format: ["cjs", "esm"], dts: true, outDir: "dist", clean: false },
  // Worker (self-contained bundle)
  { entry: ["src/sync/worker.ts"], format: ["cjs"], outDir: "dist", clean: false, noExternal: [/.*/] },
]);
```

Add the subpath export to `package.json`:

```json
{
  "exports": {
    ".": { "types": "./dist/index.d.ts", "require": "./dist/index.js", "import": "./dist/index.mjs" },
    "./sync": { "types": "./dist/sync.d.ts", "require": "./dist/sync.js", "import": "./dist/sync.mjs" }
  }
}
```

### Step 5: Add callback support (if needed)

If your sync API needs callbacks (e.g., event listeners, interceptors):

1. Add the `callbackPort` + `invokeMainThread()` to the worker (see worker section).
2. Add `handleCallback()` to the bridge (see bridge section).
3. In sync wrappers, `registerHandler()` on the bridge and pass the handler ID to the worker.
4. Buffer events during command execution, drain after.

### Pitfalls

**Signal ordering matters.** The worker must post the message to the port *before* setting the Atomics signal. `postMessage` is async (it enqueues), but `Atomics.store` + `notify` is synchronous. If the main thread wakes before the message arrives, `receiveMessageOnPort` returns `undefined`. The bridge handles this with a spin-wait, but getting the ordering right in the worker avoids the spin entirely for results.

**Reset `signal[0]` before posting callback decisions.** If you reset after posting, the worker can receive the decision, prepare the next callback, and set `signal[0] = 2` — then your reset overwrites it to `0`, causing a deadlock.

**Bundle the worker with all dependencies.** The worker runs in isolation — it can't resolve `node_modules` from the consumer's project. Use `noExternal: [/.*/]` in tsup/esbuild to bundle everything into a single file.

**Per-call MessageChannels for results.** Don't reuse a single channel for results — `receiveMessageOnPort` pulls the first message in the queue, and with concurrent calls (or buffered events using the same channel), you'd get the wrong message.

**Object registry leaks.** Objects stored in the worker registry live forever unless explicitly cleaned up. For long-running processes, implement cleanup when objects are closed/destroyed (e.g., when a page is closed, delete it from the `pages` map).

**`Atomics.wait` is only available on the main thread in Node.js.** In browsers, it's only available in workers. This pattern is Node-only. For browser environments, you'd need a different approach (e.g., Comlink with async wrappers).

**SharedArrayBuffer requires `--enable-shared-array-buffer` on older Node versions** (pre-16). On Node 16+, it's available by default.

**Worker startup is not instant.** The first `bridge.call()` may include worker initialization time (~50-100ms). This is usually acceptable since it's a one-time cost at `browser.start()`.
