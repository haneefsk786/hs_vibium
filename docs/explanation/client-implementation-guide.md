# Client Implementation Guide

> **Draft:** This is a work-in-progress draft that may be used to generate client libraries for additional languages in the future.

Reference for implementing Vibium clients in new languages (Java, C#, Ruby, Kotlin, Swift, Rust, Go, Nim, etc.).

Use the **JS client** (`clients/javascript/`) and **Python client** (`clients/python/`) as reference implementations.

> **Known issues in the Python reference:** See [#91](https://github.com/VibiumDev/vibium/issues/91), [#92](https://github.com/VibiumDev/vibium/issues/92), [#93](https://github.com/VibiumDev/vibium/issues/93), [#94](https://github.com/VibiumDev/vibium/issues/94). New clients should follow the JS client where the two diverge.

---

## Table of Contents

1. [Architecture Overview](#architecture-overview)
2. [Wire Protocol](#wire-protocol)
3. [Command Reference](#command-reference)
4. [Class Hierarchy](#class-hierarchy)
5. [Method Inventory](#method-inventory)
6. [Naming Conventions](#naming-conventions)
7. [Error Types](#error-types)
8. [Async / Sync Patterns](#async--sync-patterns)
9. [Reserved Keyword Handling](#reserved-keyword-handling)
10. [Aliases](#aliases)
11. [Binary Discovery](#binary-discovery)
12. [Testing Checklist](#testing-checklist)

---

## Architecture Overview

```
┌────────────────┐  stdin/stdout  ┌─────────────┐    BiDi/WS     ┌─────────┐
│ Client (JS,    │◄──────────────►│   vibium    │◄──────────────►│ Chrome  │
│ Python, etc.)  │ ndjson (pipes) │   binary    │ WebDriver BiDi │ browser │
└────────────────┘                └─────────────┘                └─────────┘
```

1. Client spawns the `vibium pipe` command as a subprocess
2. Client communicates via newline-delimited JSON over stdin/stdout
3. The binary sends a `vibium:ready` signal on stdout once the browser is launched
4. `vibium:` extension commands are handled by the binary; standard BiDi commands are forwarded to Chrome

### Message Format

**Request** (client → vibium):
```json
{"id": 1, "method": "vibium:page.navigate", "params": {"context": "ctx-1", "url": "https://example.com"}}
```

**Success response** (vibium → client):
```json
{"id": 1, "type": "success", "result": {}}
```

**Error response** (vibium → client):
```json
{"id": 1, "type": "error", "error": "timeout", "message": "Timeout after 30000ms waiting for '#btn'"}
```

**Event** (vibium → client, no `id`):
```json
{"method": "browsingContext.load", "params": {"context": "ctx-1", "url": "https://example.com"}}
```

---

## Wire Protocol

All extension commands use the `vibium:` prefix. Standard WebDriver BiDi commands (e.g., `browsingContext.getTree`, `session.subscribe`) are forwarded directly to Chrome.

### Command Categories

#### Element Interaction (15)
| Wire Command | Description |
|---|---|
| `vibium:click` | Click an element |
| `vibium:dblclick` | Double-click an element |
| `vibium:fill` | Fill an input field |
| `vibium:type` | Type text character by character |
| `vibium:press` | Press a key on a focused element |
| `vibium:clear` | Clear an input field |
| `vibium:check` | Check a checkbox |
| `vibium:uncheck` | Uncheck a checkbox |
| `vibium:selectOption` | Select a dropdown option |
| `vibium:hover` | Hover over an element |
| `vibium:focus` | Focus an element |
| `vibium:dragTo` | Drag an element to a target |
| `vibium:tap` | Tap an element (touch) |
| `vibium:scrollIntoView` | Scroll element into view |
| `vibium:dispatchEvent` | Dispatch a DOM event on an element |

#### Element Finding (2)
| Wire Command | Description |
|---|---|
| `vibium:find` | Find a single element |
| `vibium:findAll` | Find all matching elements |

#### Element State (13)
| Wire Command | Description |
|---|---|
| `vibium:el.text` | Get element text content |
| `vibium:el.innerText` | Get element inner text |
| `vibium:el.html` | Get element outer HTML |
| `vibium:el.value` | Get input element value |
| `vibium:el.attr` | Get element attribute |
| `vibium:el.bounds` | Get element bounding box |
| `vibium:el.isVisible` | Check if element is visible |
| `vibium:el.isHidden` | Check if element is hidden |
| `vibium:el.isEnabled` | Check if element is enabled |
| `vibium:el.isChecked` | Check if element is checked |
| `vibium:el.isEditable` | Check if element is editable |
| `vibium:el.screenshot` | Screenshot an element |
| `vibium:el.waitFor` | Wait for element state |

#### Accessibility (3)
| Wire Command | Description |
|---|---|
| `vibium:page.a11yTree` | Get accessibility tree |
| `vibium:el.role` | Get element ARIA role |
| `vibium:el.label` | Get element accessible label |

#### Page Input (11)
| Wire Command | Description |
|---|---|
| `vibium:keyboard.press` | Press a key |
| `vibium:keyboard.down` | Key down |
| `vibium:keyboard.up` | Key up |
| `vibium:keyboard.type` | Type text |
| `vibium:mouse.click` | Click at coordinates |
| `vibium:mouse.move` | Move mouse |
| `vibium:mouse.down` | Mouse button down |
| `vibium:mouse.up` | Mouse button up |
| `vibium:mouse.wheel` | Scroll mouse wheel |
| `vibium:page.scroll` | Scroll the page |
| `vibium:touch.tap` | Tap at coordinates |

#### Page Capture (2)
| Wire Command | Description |
|---|---|
| `vibium:page.screenshot` | Take a page screenshot |
| `vibium:page.pdf` | Generate PDF |

#### Page Evaluation (4)
| Wire Command | Description |
|---|---|
| `vibium:page.eval` | Evaluate JavaScript |
| `vibium:page.addScript` | Add a script tag |
| `vibium:page.addStyle` | Add a style tag |
| `vibium:page.expose` | Expose a function to the page |

#### Page Waiting (3)
| Wire Command | Description |
|---|---|
| `vibium:page.waitFor` | Wait for a selector |
| `vibium:page.wait` | Wait for a duration |
| `vibium:page.waitForFunction` | Wait for a JS function to return truthy |

#### Navigation (9)
| Wire Command | Description |
|---|---|
| `vibium:page.navigate` | Navigate to a URL |
| `vibium:page.back` | Go back |
| `vibium:page.forward` | Go forward |
| `vibium:page.reload` | Reload page |
| `vibium:page.url` | Get current URL |
| `vibium:page.title` | Get page title |
| `vibium:page.content` | Get page HTML |
| `vibium:page.waitForURL` | Wait for URL to match |
| `vibium:page.waitForLoad` | Wait for page load |

#### Lifecycle (6)
| Wire Command | Description |
|---|---|
| `vibium:browser.page` | Get the default page |
| `vibium:browser.newPage` | Create a new page |
| `vibium:browser.newContext` | Create a new browser context |
| `vibium:context.newPage` | Create a page in a context |
| `vibium:browser.pages` | List all pages |
| `vibium:browser.stop` | Stop the browser |

#### Cookie & Storage (5)
| Wire Command | Description |
|---|---|
| `vibium:context.cookies` | Get cookies |
| `vibium:context.setCookies` | Set cookies |
| `vibium:context.clearCookies` | Clear cookies |
| `vibium:context.storageState` | Get storage state |
| `vibium:context.addInitScript` | Add an init script |

#### Frame (2)
| Wire Command | Description |
|---|---|
| `vibium:page.frames` | List all frames |
| `vibium:page.frame` | Get a frame by name/URL |

#### Emulation (7)
| Wire Command | Description |
|---|---|
| `vibium:page.setViewport` | Set viewport size |
| `vibium:page.viewport` | Get viewport size |
| `vibium:page.emulateMedia` | Override CSS media features |
| `vibium:page.setContent` | Set page HTML |
| `vibium:page.setGeolocation` | Override geolocation |
| `vibium:page.setWindow` | Set window size/position |
| `vibium:page.window` | Get window info |

#### Network Interception (5)
| Wire Command | Description |
|---|---|
| `vibium:page.route` | Register a route handler |
| `vibium:network.continue` | Continue an intercepted request |
| `vibium:network.fulfill` | Fulfill an intercepted request |
| `vibium:network.abort` | Abort an intercepted request |
| `vibium:page.setHeaders` | Set extra HTTP headers |

#### WebSocket (1)
| Wire Command | Description |
|---|---|
| `vibium:page.onWebSocket` | Subscribe to WebSocket events |

#### Download & File (2)
| Wire Command | Description |
|---|---|
| `vibium:download.saveAs` | Save a download to path |
| `vibium:el.setFiles` | Set files on a file input |

#### Recording (6)
| Wire Command | Description |
|---|---|
| `vibium:recording.start` | Start recording |
| `vibium:recording.stop` | Stop recording, return trace |
| `vibium:recording.startChunk` | Start a recording chunk |
| `vibium:recording.stopChunk` | Stop a recording chunk |
| `vibium:recording.startGroup` | Start a logical group |
| `vibium:recording.stopGroup` | Stop a logical group |

#### Clock (8)
| Wire Command | Description |
|---|---|
| `vibium:clock.install` | Install fake timers |
| `vibium:clock.fastForward` | Fast-forward time |
| `vibium:clock.runFor` | Run timers for a duration |
| `vibium:clock.pauseAt` | Pause clock at a time |
| `vibium:clock.resume` | Resume clock |
| `vibium:clock.setFixedTime` | Set fixed fake time |
| `vibium:clock.setSystemTime` | Set system time |
| `vibium:clock.setTimezone` | Set timezone |

---

## Class Hierarchy

All clients must implement these classes:

```
Browser                  ← manages browser lifecycle
├── .context             ← default BrowserContext (property)
├── .keyboard            ← (accessed via Page)
├── .mouse               ← (accessed via Page)
└── .touch               ← (accessed via Page)

BrowserContext            ← cookie/storage isolation boundary
├── .recording           ← Recording (property)
└── newPage()            ← creates Page

Page                      ← a browser tab
├── .keyboard            ← Keyboard (property)
├── .mouse               ← Mouse (property)
├── .touch               ← Touch (property)
├── .clock               ← Clock (property)
├── .context             ← back-reference to BrowserContext
├── find() / findAll()   ← returns Element(s)
├── route()              ← creates Route via callback
├── onDialog()           ← creates Dialog via callback
├── onConsole()          ← creates ConsoleMessage via callback
├── onDownload()         ← creates Download via callback
├── onRequest()          ← creates Request via callback
├── onResponse()         ← creates Response via callback
└── onWebSocket()        ← creates WebSocketInfo via callback

Element                   ← a resolved DOM element
├── click/fill/type/...  ← interaction methods
├── text/html/value/...  ← state query methods
└── find() / findAll()   ← scoped element search

Keyboard                  ← page-level keyboard input
Mouse                     ← page-level mouse input
Touch                     ← page-level touch input
Clock                     ← fake timer control
Recording                 ← trace recording control
Route                     ← network interception handler
  └── .request           ← Request (property)
Dialog                    ← browser dialog (alert/confirm/prompt)
Request                   ← network request info
Response                  ← network response info
Download                  ← file download handle
ConsoleMessage            ← console.log() message
WebSocketInfo             ← WebSocket connection info
```

### Data Types

These should be structured types (interfaces/structs), not raw dicts:

| Type | Fields |
|---|---|
| `Cookie` | `name`, `value`, `domain`, `path`, `size`, `httpOnly`, `secure`, `sameSite`, `expiry?` |
| `SetCookieParam` | `name`, `value`, `domain?`, `url?`, `path?`, `httpOnly?`, `secure?`, `sameSite?`, `expiry?` |
| `StorageState` | `cookies: Cookie[]`, `origins: OriginState[]` |
| `OriginState` | `origin`, `localStorage: {name, value}[]`, `sessionStorage: {name, value}[]` |
| `BoundingBox` | `x`, `y`, `width`, `height` |
| `ElementInfo` | `tag`, `text`, `box: BoundingBox` |
| `A11yNode` | `role`, `name?`, `value?`, `description?`, `disabled?`, `expanded?`, `focused?`, `checked?`, `pressed?`, `selected?`, `level?`, `multiselectable?`, `children?: A11yNode[]` |
| `ScreenshotOptions` | `fullPage?`, `clip?: {x, y, width, height}` |
| `FindOptions` | `timeout?` |

---

## Method Inventory

### Browser

| JS | Python | Wire Command |
|---|---|---|
| `browser.start(opts?)` | `browser.start(opts?)` | *binary launch + WebSocket connect* |
| `page()` | `page()` | `vibium:browser.page` |
| `newPage()` | `new_page()` | `vibium:browser.newPage` |
| `newContext()` | `new_context()` | `vibium:browser.newContext` |
| `pages()` | `pages()` | `vibium:browser.pages` |
| `stop()` | `stop()` | `vibium:browser.stop` |
| `onPage(cb)` | `on_page(cb)` | *client-side event listener* |
| `onPopup(cb)` | `on_popup(cb)` | *client-side event listener* |
| `removeAllListeners(ev?)` | `remove_all_listeners(ev?)` | *client-side* |

### Page

| JS | Python | Wire Command |
|---|---|---|
| `go(url)` | `go(url)` | `vibium:page.navigate` |
| `back()` | `back()` | `vibium:page.back` |
| `forward()` | `forward()` | `vibium:page.forward` |
| `reload()` | `reload()` | `vibium:page.reload` |
| `url()` | `url()` | `vibium:page.url` |
| `title()` | `title()` | `vibium:page.title` |
| `content()` | `content()` | `vibium:page.content` |
| `find(sel, opts?)` | `find(sel, **opts)` | `vibium:find` |
| `findAll(sel, opts?)` | `find_all(sel, **opts)` | `vibium:findAll` |
| `screenshot(opts?)` | `screenshot(opts?)` | `vibium:page.screenshot` |
| `pdf()` | `pdf()` | `vibium:page.pdf` |
| `evaluate(expr)` | `evaluate(expr)` | `vibium:page.eval` |
| `addScript(src)` | `add_script(src)` | `vibium:page.addScript` |
| `addStyle(src)` | `add_style(src)` | `vibium:page.addStyle` |
| `expose(name, fn)` | `expose(name, fn)` | `vibium:page.expose` |
| `wait(ms)` | `wait(ms)` | `vibium:page.wait` |
| `scroll(dir?, amt?, sel?)` | `scroll(dir?, amt?, sel?)` | `vibium:page.scroll` |
| `setViewport(size)` | `set_viewport(size)` | `vibium:page.setViewport` |
| `viewport()` | `viewport()` | `vibium:page.viewport` |
| `emulateMedia(opts)` | `emulate_media(**opts)` | `vibium:page.emulateMedia` |
| `setContent(html)` | `set_content(html)` | `vibium:page.setContent` |
| `setGeolocation(coords)` | `set_geolocation(coords)` | `vibium:page.setGeolocation` |
| `setWindow(opts)` | `set_window(**opts)` | `vibium:page.setWindow` |
| `window()` | `window()` | `vibium:page.window` |
| `a11yTree(opts?)` | `a11y_tree(opts?)` | `vibium:page.a11yTree` |
| `frames()` | `frames()` | `vibium:page.frames` |
| `frame(nameOrUrl)` | `frame(name_or_url)` | `vibium:page.frame` |
| `mainFrame()` | `main_frame()` | *returns self (top frame)* |
| `bringToFront()` | `bring_to_front()` | `browsingContext.activate` |
| `close()` | `close()` | `browsingContext.close` |
| `route(pattern, handler)` | `route(pattern, handler)` | `vibium:page.route` |
| `unroute(pattern)` | `unroute(pattern)` | `network.removeIntercept` |
| `setHeaders(headers)` | `set_headers(headers)` | `vibium:page.setHeaders` |
| `onRequest(fn)` | `on_request(fn)` | *client-side event listener* |
| `onResponse(fn)` | `on_response(fn)` | *client-side event listener* |
| `onDialog(fn)` | `on_dialog(fn)` | *client-side event listener* |
| `onConsole(fn)` | `on_console(fn)` | *client-side event listener* |
| `onError(fn)` | `on_error(fn)` | *client-side event listener* |
| `onDownload(fn)` | `on_download(fn)` | *client-side event listener* |
| `onWebSocket(fn)` | `on_web_socket(fn)` | `vibium:page.onWebSocket` |
| `removeAllListeners(ev?)` | `remove_all_listeners(ev?)` | *client-side* |

### Element

| JS | Python | Wire Command |
|---|---|---|
| `click(opts?)` | `click(timeout?)` | `vibium:click` |
| `dblclick(opts?)` | `dblclick(timeout?)` | `vibium:dblclick` |
| `fill(value, opts?)` | `fill(value, timeout?)` | `vibium:fill` |
| `type(text, opts?)` | `type(text, timeout?)` | `vibium:type` |
| `press(key, opts?)` | `press(key, timeout?)` | `vibium:press` |
| `clear(opts?)` | `clear(timeout?)` | `vibium:clear` |
| `check(opts?)` | `check(timeout?)` | `vibium:check` |
| `uncheck(opts?)` | `uncheck(timeout?)` | `vibium:uncheck` |
| `selectOption(val, opts?)` | `select_option(val, timeout?)` | `vibium:selectOption` |
| `hover(opts?)` | `hover(timeout?)` | `vibium:hover` |
| `focus(opts?)` | `focus(timeout?)` | `vibium:focus` |
| `dragTo(target, opts?)` | `drag_to(target, timeout?)` | `vibium:dragTo` |
| `tap(opts?)` | `tap(timeout?)` | `vibium:tap` |
| `scrollIntoView(opts?)` | `scroll_into_view(timeout?)` | `vibium:scrollIntoView` |
| `dispatchEvent(type, init?)` | `dispatch_event(type, init?)` | `vibium:dispatchEvent` |
| `setFiles(files, opts?)` | `set_files(files, timeout?)` | `vibium:el.setFiles` |
| `text()` | `text()` | `vibium:el.text` |
| `innerText()` | `inner_text()` | `vibium:el.innerText` |
| `html()` | `html()` | `vibium:el.html` |
| `value()` | `value()` | `vibium:el.value` |
| `attr(name)` | `attr(name)` | `vibium:el.attr` |
| `bounds()` | `bounds()` | `vibium:el.bounds` |
| `isVisible()` | `is_visible()` | `vibium:el.isVisible` |
| `isHidden()` | `is_hidden()` | `vibium:el.isHidden` |
| `isEnabled()` | `is_enabled()` | `vibium:el.isEnabled` |
| `isChecked()` | `is_checked()` | `vibium:el.isChecked` |
| `isEditable()` | `is_editable()` | `vibium:el.isEditable` |
| `role()` | `role()` | `vibium:el.role` |
| `label()` | `label()` | `vibium:el.label` |
| `screenshot()` | `screenshot()` | `vibium:el.screenshot` |
| `waitUntil(state?, opts?)` | `wait_until(state?, timeout?)` | `vibium:el.waitFor` |
| `find(sel, opts?)` | `find(sel, **opts)` | `vibium:find` (scoped) |
| `findAll(sel, opts?)` | `find_all(sel, **opts)` | `vibium:findAll` (scoped) |

### BrowserContext

| JS | Python | Wire Command |
|---|---|---|
| `newPage()` | `new_page()` | `vibium:context.newPage` |
| `close()` | `close()` | `browser.removeUserContext` |
| `cookies(urls?)` | `cookies(urls?)` | `vibium:context.cookies` |
| `setCookies(cookies)` | `set_cookies(cookies)` | `vibium:context.setCookies` |
| `clearCookies()` | `clear_cookies()` | `vibium:context.clearCookies` |
| `storageState()` | `storage_state()` | `vibium:context.storageState` |
| `addInitScript(script)` | `add_init_script(script)` | `vibium:context.addInitScript` |

### Clock

| JS | Python | Wire Command |
|---|---|---|
| `install(opts?)` | `install(time?, timezone?)` | `vibium:clock.install` |
| `fastForward(ticks)` | `fast_forward(ticks)` | `vibium:clock.fastForward` |
| `runFor(ticks)` | `run_for(ticks)` | `vibium:clock.runFor` |
| `pauseAt(time)` | `pause_at(time)` | `vibium:clock.pauseAt` |
| `resume()` | `resume()` | `vibium:clock.resume` |
| `setFixedTime(time)` | `set_fixed_time(time)` | `vibium:clock.setFixedTime` |
| `setSystemTime(time)` | `set_system_time(time)` | `vibium:clock.setSystemTime` |
| `setTimezone(tz)` | `set_timezone(tz)` | `vibium:clock.setTimezone` |

### Recording

| JS | Python | Wire Command |
|---|---|---|
| `start(opts?)` | `start(opts?)` | `vibium:recording.start` |
| `stop(opts?)` | `stop(path?)` | `vibium:recording.stop` |
| `startChunk(opts?)` | `start_chunk(opts?)` | `vibium:recording.startChunk` |
| `stopChunk(opts?)` | `stop_chunk(path?)` | `vibium:recording.stopChunk` |
| `startGroup(name, opts?)` | `start_group(name, location?)` | `vibium:recording.startGroup` |
| `stopGroup()` | `stop_group()` | `vibium:recording.stopGroup` |

### Route

| JS | Python | Wire Command |
|---|---|---|
| `.request` (property) | *passed via callback args* | — |
| `fulfill(resp?)` | `fulfill(status?, headers?, ...)` | `vibium:network.fulfill` |
| `continue(overrides?)` | `continue_(overrides?)` | `vibium:network.continue` |
| `abort()` | `abort()` | `vibium:network.abort` |

### Dialog

| JS | Python | Wire Command |
|---|---|---|
| `message()` | `message()` | *from event data* |
| `type()` | `type()` | *from event data* |
| `defaultValue()` | `default_value()` | *from event data* |
| `accept(promptText?)` | `accept(prompt_text?)` | `browsingContext.handleUserPrompt` |
| `dismiss()` | `dismiss()` | `browsingContext.handleUserPrompt` |

### Request / Response / Download / ConsoleMessage / WebSocketInfo

These are lightweight data classes constructed from events. See the JS or Python source for their exact fields.

---

## Naming Conventions

### Method Names

| Convention | JS | Python | Java/Kotlin | C# | Ruby | Rust | Go |
|---|---|---|---|---|---|---|---|
| Multi-word methods | `camelCase` | `snake_case` | `camelCase` | `PascalCase` | `snake_case` | `snake_case` | `PascalCase` |
| Boolean queries | `isVisible()` | `is_visible()` | `isVisible()` | `IsVisible()` | `visible?` | `is_visible()` | `IsVisible()` |
| Setters | `setViewport()` | `set_viewport()` | `setViewport()` | `SetViewport()` | `set_viewport` / `viewport=` | `set_viewport()` | `SetViewport()` |
| Event handlers | `onDialog(fn)` | `on_dialog(fn)` | `onDialog(fn)` | `OnDialog(fn)` | `on_dialog(&block)` | `on_dialog(fn)` | `OnDialog(fn)` |

### Wire → Client Mapping

The wire protocol uses `camelCase`. Each language converts to its idiomatic style:

```
vibium:page.setViewport  →  JS: setViewport()   Python: set_viewport()   Ruby: set_viewport
vibium:el.isVisible      →  JS: isVisible()     Python: is_visible()     Ruby: visible?
vibium:page.a11yTree     →  JS: a11yTree()      Python: a11y_tree()      Ruby: a11y_tree
```

### Parameter Names

Wire parameters are `camelCase`. Convert to language idioms:

```
Wire: {"colorScheme": "dark", "reducedMotion": "reduce"}
JS:   colorScheme: "dark", reducedMotion: "reduce"     (same as wire)
Py:   color_scheme="dark", reduced_motion="reduce"     (snake_case)
Ruby: color_scheme: "dark", reduced_motion: "reduce"   (snake_case)
```

**Important:** Always convert at the client boundary. Never leak wire-protocol casing to users (see [#91](https://github.com/VibiumDev/vibium/issues/91)).

---

## Error Types

Every client must define these error types:

| Error | When Thrown |
|---|---|
| `ConnectionError` | WebSocket connection to vibium binary failed |
| `TimeoutError` | Element wait or `waitForFunction` timed out |
| `ElementNotFoundError` | Selector matched no elements |
| `BrowserCrashedError` | Browser process died unexpectedly |

### Wire Error Detection

The wire protocol returns errors in this format:

```json
{"id": 1, "type": "error", "error": "timeout", "message": "Timeout after 30000ms waiting for '#btn'"}
```

Map the `error` field to structured error types:
- `"timeout"` → `TimeoutError`
- Messages containing `"not found"` or `"no elements"` → `ElementNotFoundError`
- WebSocket close with no response → `BrowserCrashedError`
- WebSocket connection failure → `ConnectionError`

### Language-Specific Names

Some languages have built-in `TimeoutError` or `ConnectionError`. Use prefixed names to avoid conflicts:

| Language | Timeout | Connection |
|---|---|---|
| JS/TS | `TimeoutError` | `ConnectionError` |
| Python | `VibiumTimeoutError` | `VibiumConnectionError` |
| Java | `VibiumTimeoutException` | `VibiumConnectionException` |
| C# | `VibiumTimeoutException` | `VibiumConnectionException` |
| Ruby | `TimeoutError` | `ConnectionError` (namespaced under `Vibium::`) |
| Rust | `Error::Timeout` | `Error::Connection` (enum variants) |
| Go | `ErrTimeout` | `ErrConnection` (sentinel errors) |

---

## Async / Sync Patterns

### Every client must have an async API

The wire protocol is inherently async (WebSocket messages). The primary API should be async.

### Sync wrappers are optional but recommended

For scripting and REPL use, a sync wrapper dramatically improves the getting-started experience.

| Language | Async Pattern | Sync Pattern |
|---|---|---|
| JS/TS | `async/await` (native) | Separate `*Sync` classes |
| Python | `async/await` | Separate `sync_api/` module (blocks on event loop) |
| Java | `CompletableFuture<T>` | Blocking `.get()` wrappers |
| Kotlin | `suspend fun` (coroutines) | `runBlocking { }` wrappers |
| C# | `Task<T>` / `async` | `.GetAwaiter().GetResult()` wrappers |
| Ruby | Not needed (GIL) | Primary API is sync; use threads for events |
| Rust | `async fn` (tokio/async-std) | `block_on()` wrappers |
| Go | Goroutines (inherently concurrent) | Primary API is sync with channels for events |
| Swift | `async/await` (structured concurrency) | Sync wrappers with `DispatchSemaphore` |

### Event Handling

Events (`onDialog`, `onRequest`, etc.) are received as WebSocket messages with no `id`. The client must:

1. Parse incoming messages
2. If `type` is `"success"` or `"error"` → match to pending request by `id`
3. If `method` is present (event) → dispatch to registered listeners

---

## Reserved Keyword Handling

Some method names conflict with language reserved words. Here's how to handle them:

| Wire Method | Conflict | Resolution |
|---|---|---|
| `vibium:network.continue` | `continue` is reserved in most languages | Python: `continue_()`, Java: `doContinue()`, Ruby: `continue_request`, C#: `Continue()` (C# allows PascalCase), Rust: `r#continue()` or `continue_()`, Go: `Continue()` |

### General Rules

1. **Append underscore** (Python, Ruby): `continue_()`, `import_()`
2. **Prefix with `do`** (Java, Kotlin): `doContinue()`
3. **Raw identifier** (Rust): `r#continue()`
4. **PascalCase avoids most conflicts** (C#, Go)

---

## Aliases

The JS client provides some aliases for Playwright compatibility and discoverability. New clients should include these:

| Primary | Alias | Reason |
|---|---|---|
| `attr(name)` | `getAttribute(name)` | Playwright compat |
| `bounds()` | `boundingBox()` | Playwright compat |
| `go(url)` | — | Short and memorable; `navigate` is the wire name |
| `waitUntil(state)` | — | Maps to `vibium:el.waitFor` on wire |

### Which to Include

- **Always include the primary name** (shorter, Vibium-native)
- **Include Playwright aliases** for `getAttribute` and `boundingBox` — many users come from Playwright
- **Do not** alias everything — keep the API surface small

---

## Binary Discovery

Each client needs to find and launch the `vibium` binary. The resolution order:

1. **Environment variable** `VIBIUM_BIN_PATH` — highest priority
2. **PATH lookup** — `which vibium` / `where vibium`
3. **npm-installed binary** — check `node_modules/.bin/vibium`
4. **Known install locations** — platform-specific defaults

### Reference

- JS: `clients/javascript/src/clicker/binary.ts` → `getVibiumBinPath()`
- Python: `clients/python/src/vibium/binary.py` → `find_vibium_bin()`

---

## Testing Checklist

Before releasing a new client, verify:

- [ ] `browser.start()` launches a visible browser
- [ ] `browser.start(headless=True)` launches headless
- [ ] `page.go(url)` navigates and waits for load
- [ ] `page.find("selector")` returns an Element
- [ ] `element.click()` performs a click
- [ ] `element.fill("text")` fills an input
- [ ] `page.screenshot()` returns image bytes
- [ ] `page.evaluate("1 + 1")` returns `2`
- [ ] `context.cookies()` / `setCookies()` round-trips
- [ ] `page.route()` intercepts and can fulfill requests
- [ ] `page.onDialog()` handles alert/confirm/prompt
- [ ] Error types are raised (timeout, element not found)
- [ ] `browser.stop()` cleanly shuts down
- [ ] Binary discovery works via `VIBIUM_BIN_PATH` and PATH
- [ ] Sync wrapper works (if provided)

Run the existing test suite against your client:

```bash
make test  # runs CLI + JS + MCP + Python tests
```
